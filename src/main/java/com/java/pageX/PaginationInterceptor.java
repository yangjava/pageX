package com.java.pageX;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.RowBounds;

import com.java.pageX.exception.PageXException;
import com.java.pageX.kits.StringUtils;
import com.java.pageX.pagination.DialectFactory;
import com.java.pageX.pagination.IDialect;
import com.java.pageX.pagination.Pagination;

@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = {
		Connection.class, Integer.class }) })
public class PaginationInterceptor implements Interceptor {

	/* 溢出总页数，设置第一页 */
	private boolean overflowCurrent = false;

	/* 方言类型 */
	private String dialectType;

	/* 方言实现类 */
	private String dialectClazz;

	public Object intercept(Invocation invocation) throws Throwable {
		Object target = invocation.getTarget();

		if (target instanceof StatementHandler) {
			StatementHandler statementHandler = (StatementHandler) target;
			// MetaObject是Mybatis提供的一个的工具类，通过它包装一个对象后可以获取或设置该对象的原本不可访问的属性
			MetaObject metaStatementHandler = SystemMetaObject
					.forObject(statementHandler);
			// 获取statementHandler的属性 BaseStatementHandler
			RowBounds rowBounds = (RowBounds) metaStatementHandler
					.getValue("delegate.rowBounds");

			/* 不需要分页的场合 */
			if (rowBounds == null || rowBounds == RowBounds.DEFAULT) {
				return invocation.proceed();
			}

			/* 定义数据库方言 */
			IDialect dialect = null;
			if (StringUtils.isNotEmpty(dialectType)) {
				dialect = DialectFactory.getDialectByDbtype(dialectType);
			} else {
				if (StringUtils.isNotEmpty(dialectClazz)) {
					try {
						Class<?> clazz = Class.forName(dialectClazz);
						if (IDialect.class.isAssignableFrom(clazz)) {
							dialect = (IDialect) clazz.newInstance();
						}
					} catch (ClassNotFoundException e) {
						throw new PageXException("Class :" + dialectClazz
								+ " is not found");
					}
				}
			}

			/* 未配置方言则抛出异常 */
			if (dialect == null) {
				throw new PageXException(
						"The value of the dialect property in mybatis configuration.xml is not defined.");
			}

			/*
			 * <p> 禁用内存分页 </p> <p> 内存分页会查询所有结果出来处理（这个很吓人的），如果结果变化频繁这个数据还会不准。
			 * </p>
			 */
			BoundSql boundSql = (BoundSql) metaStatementHandler
					.getValue("delegate.boundSql");
			String originalSql = (String) boundSql.getSql();
			metaStatementHandler.setValue("delegate.rowBounds.offset",
					RowBounds.NO_ROW_OFFSET);
			metaStatementHandler.setValue("delegate.rowBounds.limit",
					RowBounds.NO_ROW_LIMIT);

			/**
			 * <p>
			 * 分页逻辑
			 * </p>
			 * <p>
			 * 查询总记录数 count
			 * </p>
			 */
			if (rowBounds instanceof Pagination) {
				MappedStatement mappedStatement = (MappedStatement) metaStatementHandler
						.getValue("delegate.mappedStatement");
				Connection connection = (Connection) invocation.getArgs()[0];
				Pagination page = (Pagination) rowBounds;
				boolean orderBy = true;
				if (page.isSearchCount()) {
					/*
					 * COUNT 查询，去掉 ORDER BY 优化执行 SQL
					 */
					StringBuffer countSql = new StringBuffer(
							"SELECT COUNT(1) AS TOTAL ");
					String tempSql = originalSql.toUpperCase();
					int formIndex = -1;
					if (page.isOptimizeCount()) {
						formIndex = tempSql.indexOf("FROM");
					}
					int orderByIndex = tempSql.lastIndexOf("ORDER BY");
					if (orderByIndex > -1) {
						orderBy = false;
						tempSql = originalSql.substring(0, orderByIndex);
					}
					if (page.isOptimizeCount() && formIndex > -1) {
						countSql.append(tempSql.substring(formIndex));
					} else {
						countSql.append("FROM (").append(tempSql).append(") A");
					}
					page = this.count(countSql.toString(), connection,
							mappedStatement, boundSql, page);
					/** 总数 0 跳出执行 */
					if (page.getTotal() <= 0) {
						return invocation.proceed();
					}
				}

				/* 执行 SQL */
				StringBuffer buildSql = new StringBuffer(originalSql);
				if (orderBy && null != page.getOrderByField()) {
					buildSql.append(" ORDER BY ")
							.append(page.getOrderByField());
					buildSql.append(page.isAsc() ? " ASC " : " DESC ");
				}
				originalSql = dialect.buildPaginationSql(buildSql.toString(),
						page.getOffsetCurrent(), page.getSize());
			}

			/**
			 * 查询 SQL 设置
			 */
			metaStatementHandler.setValue("delegate.boundSql.sql", originalSql);
		}

		return invocation.proceed();
	}

	/**
	 * 查询总记录条数
	 * 
	 * @param sql
	 * @param connection
	 * @param mappedStatement
	 * @param boundSql
	 * @param page
	 */
	public Pagination count(String sql, Connection connection,
			MappedStatement mappedStatement, BoundSql boundSql, Pagination page) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = connection.prepareStatement(sql);
			BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(),
					sql, boundSql.getParameterMappings(),
					boundSql.getParameterObject());
			ParameterHandler parameterHandler = new DefaultParameterHandler(
					mappedStatement, boundSql.getParameterObject(), countBS);
			parameterHandler.setParameters(pstmt);
			rs = pstmt.executeQuery();
			int total = 0;
			if (rs.next()) {
				total = rs.getInt(1);
			}
			page.setTotal(total);
			/*
			 * 溢出总页数，设置第一页
			 */
			if (overflowCurrent && (page.getCurrent() > page.getPages())) {
				page = new Pagination(1, page.getSize());
				page.setTotal(total);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return page;
	}

	@Override
	public Object plugin(Object target) {
		if (target instanceof StatementHandler) {
			return Plugin.wrap(target, this);
		}
		return target;
	}

	@Override
	public void setProperties(Properties prop) {
		// 获取参数dialectType
		String dialectType = prop.getProperty("dialectType");
		// 获取参数dialectClazz
		String dialectClazz = prop.getProperty("dialectClazz");
		if (StringUtils.isNotEmpty(dialectType)) {
			this.dialectType = dialectType;
		}
		if (StringUtils.isNotEmpty(dialectClazz)) {
			this.dialectClazz = dialectClazz;
		}
	}

	public String getDialectType() {
		return dialectType;
	}

	public void setDialectType(String dialectType) {
		this.dialectType = dialectType;
	}

	public String getDialectClazz() {
		return dialectClazz;
	}

	public void setDialectClazz(String dialectClazz) {
		this.dialectClazz = dialectClazz;
	}

}
