package com.java.pageX.pagination.dialects;

import com.java.pageX.pagination.IDialect;

/**
 * 
 * @author:杨京京
 * @QQ:1280025885
 */
public class OracleDialect implements IDialect {

	public String buildPaginationSql(String originalSql, int offset, int limit) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( ");
		sql.append(originalSql).append(" ) TMP WHERE ROWNUM <=").append((offset >= 1) ? (offset + limit) : limit);
		sql.append(") WHERE ROW_ID > ").append(offset);
		return sql.toString();
	}

}
