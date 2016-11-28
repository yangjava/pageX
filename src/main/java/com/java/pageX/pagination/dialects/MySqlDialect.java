package com.java.pageX.pagination.dialects;

import com.java.pageX.pagination.IDialect;

/**
 * 
 * @author:杨京京
 * @QQ:1280025885
 */
public class MySqlDialect implements IDialect {

	public String buildPaginationSql(String originalSql, int offset, int limit) {
		StringBuilder sql = new StringBuilder(originalSql);
		sql.append(" LIMIT ").append(offset).append(",").append(limit);
		return sql.toString();
	}

}
