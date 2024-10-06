package com.lyc.common.pdfimage.exception;

/**
 * @author : 		刘勇成
 * @description : pdf转换异常实例
 * @date : 		2023/10/12 15:27
 */
public class PdfConvertException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private Object data;
	protected Integer code;
	protected String type;
	protected String msgDetail;

	public PdfConvertException() {
		super();
	}

	public PdfConvertException(String message) {
		super(message);
	}

	public PdfConvertException(Throwable cause) {
		super(cause);
	}

	public PdfConvertException(Integer code, String message) {
		super(message);
		this.code = code;
	}

	public PdfConvertException(Integer code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public PdfConvertException(Integer code, String type, String message, String msgDetail, Throwable cause, Object data) {
		super(message, cause);
		this.code = code;
		this.type = type;
		this.data = data;
		this.msgDetail = msgDetail;
	}

	public PdfConvertException(String message, Throwable cause) {
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
