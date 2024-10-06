package com.lyc.common.pdfimage.formatCheck.model;

import com.lyc.common.pdfimage.formatCheck.handler.AbstractCheckHandler;
import com.lyc.common.pdfimage.model.monitor.MonitorRequest;
import com.lyc.common.pdfimage.model.monitor.MonitorResponse;

import java.util.concurrent.CompletableFuture;

/**
 * @description :
 * pdfbox格式校验请求
 * @author : 		刘勇成
 * @date : 		2023/10/10 11:33
 *
 * @param
 * @return
 */
public class PbRenderExceptionRequest extends MonitorRequest<Throwable> {

	private final AbstractCheckHandler build;

	public PbRenderExceptionRequest(Throwable body, long internalLockLeaseTime, AbstractCheckHandler build) {
		super(body, internalLockLeaseTime);
		this.build = build;
	}

	public AbstractCheckHandler getBuild() {
		return build;
	}

	@Override
	public CompletableFuture<MonitorResponse<Throwable>> submitRequest() throws Throwable {
		build.doHandler(this);
		return null;
	}

	@Override
	public boolean toCancel() throws Throwable {
		return false;
	}

	@Override
	public boolean toCheck() throws Throwable {
		return false;
	}
}
