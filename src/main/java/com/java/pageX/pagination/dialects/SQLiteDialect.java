package com.java.pageX.pagination.dialects;

import com.java.pageX.pagination.IDialect;

/**
 * 
 * @author:杨京京
 * @QQ:1280025885
 */
public class SQLiteDialect implements IDialect {

	public String buildPaginationSql(String originalSql, int offset, int limit) {
		StringBuffer sql = new StringBuffer(originalSql);
		sql.append(" limit ").append(limit).append(" offset ").append(offset);
		return sql.toString();
	}

}
