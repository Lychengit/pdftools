package com.lyc.common.pdfimage.service;

import com.lyc.common.pdfimage.model.PdfRendererTask;

/**
 * @author : 		刘勇成
 * @description : 转换实例相关回调接口
 *
 * @date : 		2023/3/4 10:43
 */
public interface PdfRendererCallable<T> {

	/**
	 * 获取初始化的转换实例
 	 */
	T initPdfRenderer(PdfRendererTask task);
}
