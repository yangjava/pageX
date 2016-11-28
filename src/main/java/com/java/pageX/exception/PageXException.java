
package com.java.pageX.exception;
/**
 * 
 * @author:杨京京
 * @QQ:1280025885
 */
public class PageXException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PageXException(String message) {
		super(message);
	}

	public PageXException(Throwable throwable) {
		super(throwable);
	}

	public PageXException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
