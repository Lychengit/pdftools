package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.service.PdfRendererCallable;

/**
 * @author : 		刘勇成
 * @description : 转换实例相关回调任务（如用于回调初始化转换实例）
 * @date : 		2023/3/4 10:52
 */
public class PdfRendererTask<T> {

	private String pdfKey;
	private Long pdfSize;
	private Long pdfNum;

	// 转换实例相关回调实例
	private PdfRendererCallable<T> callable;

	public PdfRendererTask(PdfRendererCallable<T> callable) {
		if (callable == null) {
			throw new RuntimeException("PdfRendererCallable实例不能为空");
		}
		this.callable = callable;
	}

	public String getPdfKey() {
		return pdfKey;
	}

	public void setPdfKey(String pdfKey) {
		this.pdfKey = pdfKey;
	}

	public Long getPdfSize() {
		return pdfSize;
	}

	public void setPdfSize(Long pdfSize) {
		this.pdfSize = pdfSize;
	}

	public PdfRendererCallable<T> getCallable() {
		return callable;
	}

	public Long getPdfNum() {
		return pdfNum;
	}

	public void setPdfNum(Long pdfNum) {
		this.pdfNum = pdfNum;
	}
}
