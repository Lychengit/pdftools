package com.lyc.common.pdfimage.exception;

public class RendererNotValidException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RendererNotValidException() {
		super();
	}

	public RendererNotValidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RendererNotValidException(String message, Throwable cause) {
		super(message, cause);
	}

	public RendererNotValidException(String message) {
		super(message);
	}

	public RendererNotValidException(Throwable cause) {
		super(cause);
	}

}
