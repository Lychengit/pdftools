package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.model.thread.MonitorLongPollingRunnable;
import org.apache.pdfbox.cos.COSName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description :
 *
 * 监控和清理pdfbox的COSName的nameMap属性值内存泄漏的问题
 *
 * pdfbox：https://issues.apache.org/jira/browse/PDFBOX-5731
 *
 * @author : 		刘勇成
 * @date : 		2023/1/6 10:09
 *
 * @param
 * @return
 */
public class PbCOSNameMapCleanWorker  extends MonitorLongPollingRunnable {
	protected static Logger logger = LoggerFactory.getLogger(PdfboxGroupWorker.class);

	public PbCOSNameMapCleanWorker(long waitForRunningTime, String serviceName) {
		super(waitForRunningTime, serviceName);
	}

	@Override
	protected void doCommit() {
		if (orCheck()) {
			logger.info("清理了一次pdfbox的COSName的nameMap集合");
			COSName.clearResources();
			logger.info("清理pdfbox的COSName的nameMap集合完成");
		} else {
			logger.info("忽略清理pdfbox的COSName的nameMap集合");
		}
	}

	private boolean orCheck(){
		return true;
	}

}
