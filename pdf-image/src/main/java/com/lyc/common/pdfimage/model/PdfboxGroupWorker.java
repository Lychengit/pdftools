package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.PdfBoxConvertorProvider;
import com.lyc.common.pdfimage.model.thread.MonitorLongPollingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : 		刘勇成
 * @description : 长轮询任务（每60s执行，可调用putRequest()提前唤醒轮询线程）
 * @date : 		2023/9/15 17:03
 */
public class PdfboxGroupWorker extends MonitorLongPollingRunnable {

	protected static Logger logger = LoggerFactory.getLogger(PdfboxGroupWorker.class);

	public PdfboxGroupWorker(long waitForRunningTime, String serviceName) {
		super(waitForRunningTime, serviceName);
	}

	@Override
	protected void doCommit() {
		try {
			PdfBoxConvertorProvider.clearNoUseRenderer();
			Thread.sleep(100);
		} catch (Throwable e) {
			logger.error("回收无用pdfbox转换实例异常：", e);
		}
	}
}
