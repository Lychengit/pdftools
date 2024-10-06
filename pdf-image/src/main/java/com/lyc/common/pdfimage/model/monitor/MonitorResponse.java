package com.lyc.common.pdfimage.model.monitor;

import com.lyc.common.pdfimage.model.HttpStatus;

/**
 * @author : 		刘勇成
 * @description : 监控相关的监控响应类
 * @date : 		2022/11/10 17:45
 */
public class MonitorResponse<T> {

	private final T body;

	private HttpStatus code;

	private String message;

	private Throwable throwable;

	public MonitorResponse(T body) {
		this.body = body;
	}

	public T getBody() {
		return body;
	}

	public HttpStatus getCode() {
		return code;
	}

	public void setCode(HttpStatus code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public boolean isSuccess(){
		return HttpStatus.OK.equals(this.code);
	}
}