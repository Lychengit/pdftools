package com.lyc.common.pdfimage.service;

import com.lyc.common.pdfimage.model.CachePdfboxRendererGroup;
import com.lyc.common.pdfimage.model.PbConvertParamConfig;
import com.lyc.common.pdfimage.model.SingletonExecutor;

import java.util.List;

/**
 * @author : 		刘勇成
 * @description : pdfbox多转换实例模式下负载均衡的相关接口
 * @date : 		2023/4/4 11:15
 */
public interface PdfboxAbstractLoadBalance {

	/**
	 * 从多转换实例里面负载一个转换实例转换
	 * @param group
	 * @return
	 */
	String selectPreviewid(CachePdfboxRendererGroup group, List<SingletonExecutor> selects, PbConvertParamConfig convertParamConfig);

	/**
	 * 从转换队列负载一个队列
	 * @return
	 */
	int selectQueueNo(List<SingletonExecutor> selects, PbConvertParamConfig convertParamConfig);
}
