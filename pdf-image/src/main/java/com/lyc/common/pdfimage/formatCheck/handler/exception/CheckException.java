package com.lyc.common.pdfimage.formatCheck.handler.exception;

/**
 *
 */
public class CheckException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private Object data;
	protected Integer code;
	protected String type;
	protected String msgDetail;

	public CheckException() {
		super();
	}

	public CheckException(String message) {
		super(message);
	}

	public CheckException(Throwable cause) {
		super(cause);
	}

	public CheckException(Integer code, String message) {
		super(message);
		this.code = code;
	}

	public CheckException(Integer code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public CheckException(Integer code, String type, String message, String msgDetail, Throwable cause, Object data) {
		super(message, cause);
		this.code = code;
		this.type = type;
		this.data = data;
		this.msgDetail = msgDetail;
	}

	public CheckException(String message, Throwable cause) {
		super(message, cause);
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMsgDetail() {
		return msgDetail;
	}

	public void setMsgDetail(String msgDetail) {
		this.msgDetail = msgDetail;
	}
}
