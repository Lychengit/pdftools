package com.lyc.common.pdfimage.model;

import io.netty.util.HashedWheelTimer;

/**
 * @description : Pdfbox转换监控相关参数信息
 *
 * @author : 		刘勇成
 * @date : 		2023/2/28 11:44
 *
 * @param
 * @return
 */
public class PdfboxConvertMessage {

	private CachePdfboxRenderer pdfRenderer;
	private final String previewid;
	private final String previewidChild;
	private final int pageNo;
	private final HashedWheelTimer hashedWheelTimer;

	public PdfboxConvertMessage(String previewid, String previewidChild, int pageNo, HashedWheelTimer hashedWheelTimer) {
		this.previewid = previewid;
		this.previewidChild = previewidChild;
		this.pageNo = pageNo;
		this.hashedWheelTimer = hashedWheelTimer;
	}

	public void setPdfRenderer(CachePdfboxRenderer pdfRenderer) {
		this.pdfRenderer = pdfRenderer;
	}

	public CachePdfboxRenderer getPdfRenderer() {
		return pdfRenderer;
	}

	public String getPreviewid() {
		return previewid;
	}

	public String getPreviewidChild() {
		return previewidChild;
	}

	public int getPageNo() {
		return pageNo;
	}

	public HashedWheelTimer getHashedWheelTimer() {
		return hashedWheelTimer;
	}
}
