package com.java.pageX.kits;

public class StringUtils {
	/**
	 * 空字符串
	 */
	public static final String EMPTY_STRING = "";

	/**
	 * <p>
	 * 判断字符串是否为空
	 * </p>
	 *
	 * @param str
	 *            需要判断字符串
	 * @return 判断结果
	 */
	public static boolean isEmpty(String str) {
		return str == null || EMPTY_STRING.equals(str.trim());
	}

	/**
	 * <p>
	 * 判断字符串是否不为空
	 * </p>
	 *
	 * @param str
	 *            需要判断字符串
	 * @return 判断结果
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
}
