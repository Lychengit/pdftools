package com.lyc.common.pdfimage.model.monitor;

import com.lyc.common.pdfimage.model.HttpStatus;
import com.lyc.common.pdfimage.utils.TimeUtils;

import java.util.concurrent.CompletableFuture;

/**
 * @description : 
 * 监控相关的监控请求类
 * @author : 		刘勇成
 * @date : 		2022/11/17 18:59
 *
 * @param 
 * @return 
 */
public abstract class MonitorRequest<T> {

	private final T body;
	private CompletableFuture<MonitorResponse<T>> monitorOKFuture = new CompletableFuture<>();
	private OptRequest optRequest = new OptRequest();

	// 请求携带过期时间
	private final long internalLockLeaseTime;
	private volatile long expireTime;

	// 描述当前请求的信息。
	private String msg;

	public MonitorRequest(T body, long internalLockLeaseTime) {
		this.body = body;
		this.internalLockLeaseTime = internalLockLeaseTime;
		continueExpireTime();
	}

	public T getBody() {
		return body;
	}

	public boolean cancel(){
		return optRequest.cancel();
	}

	public boolean isCancelled() {
		return optRequest.isCancelled();
	}

	public long getInternalLockLeaseTime() {
		return internalLockLeaseTime;
	}

	public boolean isExpireTime(){
		return this.getExpireTime() < System.currentTimeMillis();
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void continueExpireTime(){
		setExpireTime(TimeUtils.countExpireTime(getInternalLockLeaseTime()));
	}

	public void wakeupCustomerOk(final T putMessage) {
		MonitorResponse<T> response = new MonitorResponse<>(putMessage);
		response.setCode(HttpStatus.OK);
		response.setMessage("");
		this.monitorOKFuture.complete(response);
	}

	public void wakeupCustomer400(final T putMessage, String message, Throwable throwable) {
		MonitorResponse<T> response = new MonitorResponse<>(putMessage);
		response.setCode(HttpStatus.BAD_REQUEST);
		response.setMessage(message);
		response.setThrowable(throwable);
		this.monitorOKFuture.complete(response);
	}

	public void wakeupCustomer(final T putMessage, HttpStatus code, String message, Throwable throwable) {
		MonitorResponse<T> response = new MonitorResponse<>(putMessage);
		response.setCode(code);
		response.setMessage(message);
		response.setThrowable(throwable);
		this.monitorOKFuture.complete(response);
	}

	public CompletableFuture<MonitorResponse<T>> future() {
		return this.monitorOKFuture;
	}

	public abstract CompletableFuture<MonitorResponse<T>> submitRequest() throws Throwable;

	public boolean cancelMonitor() throws Throwable {

		try {
			// 如果还没有结果则需要再执行回查操作
			if (!this.future().isDone()){
				toCheck();
			}

			try {
				// 取消监控任务
				cancelRequest();
			} finally {
				if (!this.future().isDone()) {
					this.wakeupCustomerOk(this.getBody());
				}
			}

			return true;
		} finally {
			// 最后保证取消监控任务以及响应ok
			try {
				if (!isCancelled()) {
					cancelRequest();
				}
			} finally {
				if (!this.future().isDone()) {
					this.wakeupCustomerOk(this.getBody());
				}
			}
		}

	}

	private void cancelRequest() throws Throwable {
		try {
			cancel();
		} finally {
			toCancel();
		}
	}

	// 取消动作
	public abstract boolean toCancel() throws Throwable;

	// 回查任务动作
	public abstract boolean toCheck() throws Throwable;

}
