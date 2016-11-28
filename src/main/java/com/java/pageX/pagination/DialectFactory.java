package com.java.pageX.pagination;

import com.java.pageX.pagination.dialects.HSQLDialect;
import com.java.pageX.pagination.dialects.MySqlDialect;
import com.java.pageX.pagination.dialects.OracleDialect;
import com.java.pageX.pagination.dialects.PostgreDialect;
import com.java.pageX.pagination.dialects.SQLServerDialect;
import com.java.pageX.pagination.dialects.SQLiteDialect;


/**
 * 
 * @author:杨京京
 * @QQ:1280025885
 */
public class DialectFactory {

	/**
	 * <p>
	 * 根据数据库类型选择不同分页方言
	 * </p>
	 * 
	 * @param dbtype
	 *            数据库类型
	 * @return
	 * @throws Exception
	 */
	public static IDialect getDialectByDbtype( String dbtype ) throws Exception {
		if ( "mysql".equalsIgnoreCase(dbtype) ) {
			return new MySqlDialect();
		} else if ( "oracle".equalsIgnoreCase(dbtype) ) {
			return new OracleDialect();
		} else if ( "hsql".equalsIgnoreCase(dbtype) ) {
			return new HSQLDialect();
		} else if ( "sqlite".equalsIgnoreCase(dbtype) ) {
			return new SQLiteDialect();
		} else if ( "postgre".equalsIgnoreCase(dbtype) ) {
			return new PostgreDialect();
		} else if ( "sqlserver".equalsIgnoreCase(dbtype) ) {
			return new SQLServerDialect();
		} else {
			return null;
		}
	}

}
