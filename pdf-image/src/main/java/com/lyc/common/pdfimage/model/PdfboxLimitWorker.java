package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.PdfBoxConvertorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : 		刘勇成
 * @description : 长轮询任务（每10s执行，可调用putRequest()提前唤醒轮询线程）
 * @date : 		2023/9/15 17:03
 */
public class PdfboxLimitWorker extends MonitorLongPollingRunnable {

	protected static Logger logger = LoggerFactory.getLogger(PdfboxLimitWorker.class);

	public PdfboxLimitWorker(long waitForRunningTime, String serviceName) {
		super(waitForRunningTime, serviceName);
	}

	@Override
	protected void doCommit() {
		try {
			PdfBoxConvertorProvider.monitorTakings();
			Thread.sleep(100);
		} catch (Throwable e) {
			logger.error("监控和唤醒占位线程出现异常", e);
		}
	}
}
