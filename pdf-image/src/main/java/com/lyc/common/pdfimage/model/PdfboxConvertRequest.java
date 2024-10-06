package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.PdfBoxConvertorProvider;
import com.lyc.common.pdfimage.model.monitor.MonitorRequest;
import com.lyc.common.pdfimage.model.monitor.MonitorResponse;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author : 		刘勇成
 * @description : Pdfbox转换超时监控请求
 *
 * @date : 		2022/12/13 10:52
 */
public class PdfboxConvertRequest extends MonitorRequest<PdfboxConvertMessage> {

	private static final Logger log = LoggerFactory.getLogger(PdfboxConvertRequest.class);

	private volatile Timeout timeout;

	public PdfboxConvertRequest(PdfboxConvertMessage body, long internalLockLeaseTime) {
		super(body, internalLockLeaseTime);
		if (body == null){
			throw new RuntimeException("PdfboxConvertMessage对象不能为空");
		}
	}

	public Timeout getTimeout() {
		return timeout;
	}

	public void setTimeout(Timeout timeout) {
		this.timeout = timeout;
	}

	@Override
	public CompletableFuture<MonitorResponse<PdfboxConvertMessage>> submitRequest() throws Throwable {
		monitor(this);
		return this.future();
	}

	@Override
	public boolean toCancel() throws Throwable {
		if (timeout != null){
			timeout.cancel();
		}
		return true;
	}

	@Override
	public boolean toCheck() throws Throwable {
		return check(this);
	}

	private static void monitor(PdfboxConvertRequest request){
		checkParam(request);
		PdfboxConvertMessage body = request.getBody();
		HashedWheelTimer hashedWheelTimer = body.getHashedWheelTimer();
		if (hashedWheelTimer == null){
			throw new RuntimeException("PdfboxConvertMessage的hashedWheelTimer为null");
		}

		Timeout timeout = hashedWheelTimer.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {

				// 如果当前转换已完成，则直接返回
				if (request.isCancelled()) {
					return;
				}

				// 如果转换超时，则关闭当前PdDocument实例，中断文档加载或转换过程
				if (checkExpireTime(request)){
					return;
				}

				monitor(request);
			}
		}, request.getInternalLockLeaseTime() / 3, TimeUnit.MILLISECONDS);

		request.setTimeout(timeout);
	}

	private static boolean check(PdfboxConvertRequest request) throws Throwable {
		checkParam(request);
		if (checkExpireTime(request)) {
			return false;
		}
		return true;
	}

	private static void checkParam(PdfboxConvertRequest request) {
		PdfboxConvertMessage body = request.getBody();
		if (body == null){
			throw new RuntimeException("PdfboxConvertRequest请求的参数PdfboxConvertMessage为null");
		}
		CachePdfboxRenderer pdfRenderer = body.getPdfRenderer();
		if (pdfRenderer == null){
			throw new RuntimeException("PdfboxConvertRequest请求的参数pdfRenderer为null");
		}
	}

	private static boolean checkExpireTime(PdfboxConvertRequest request) {
		PdfboxConvertMessage body = request.getBody();
		CachePdfboxRenderer pdfRenderer = body.getPdfRenderer();
		if (request.getExpireTime() < System.currentTimeMillis()) {
			try {
				PdfBoxConvertorProvider.cancelRenderer(pdfRenderer);
				log.warn("pdfbox当前转换任务【{}】的步骤超时，强制中断转换任务，关闭pdDocument！预览id【{}】, 当前页【{}】", request.getMsg(), body.getPreviewid(), body.getPageNo());
			} finally {
				log.error("【"+ request.getMsg() +"】执行超时，当前设置的超时时间：【"+ request.getInternalLockLeaseTime() + "】ms，预览id【"+ body.getPreviewid() +"】, 当前页【"+body.getPageNo() +"】");
				request.wakeupCustomer400(body, "当前图片预览转换超时，当前设置的超时时间：【"+ request.getInternalLockLeaseTime() + "】ms", null);
			}
			return true;
		}
		return false;
	}
}
