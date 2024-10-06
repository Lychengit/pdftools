package com.lyc.common.pdfimage.exception;

public class ConvertorException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConvertorException() {
		super();
	}

	public ConvertorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConvertorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConvertorException(String message) {
		super(message);
	}

	public ConvertorException(Throwable cause) {
		super(cause);
	}

}
