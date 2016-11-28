package com.java.pageX.pagination.dialects;

import com.java.pageX.pagination.IDialect;

/**
 * 
 * @author:杨京京
 * @QQ:1280025885
 */
public class SQLServerDialect implements IDialect {

	public String buildPaginationSql( String originalSql, int offset, int limit ) {
		StringBuffer sql = new StringBuffer(originalSql);
		sql.append(" OFFSET ").append(offset).append(" ROWS FETCH NEXT ");
		sql.append(limit).append(" ROWS ONLY");
		return sql.toString();
	}

}
