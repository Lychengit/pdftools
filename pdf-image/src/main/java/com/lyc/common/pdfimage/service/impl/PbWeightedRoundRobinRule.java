package com.lyc.common.pdfimage.service.impl;

import com.lyc.common.pdfimage.model.CachePdfboxRendererGroup;
import com.lyc.common.pdfimage.model.PbConvertParamConfig;
import com.lyc.common.pdfimage.model.SingletonExecutor;
import com.lyc.common.pdfimage.service.PdfboxAbstractLoadBalance;

import java.util.List;

/**
 * @author : 		刘勇成
 * @description : 加权轮询算法
 * @date : 		2023/4/4 11:44
 */
public class PbWeightedRoundRobinRule implements PdfboxAbstractLoadBalance {
	@Override
	public String selectPreviewid(CachePdfboxRendererGroup group, List<SingletonExecutor> selects, PbConvertParamConfig convertParamConfig) {
		return null;
	}

	@Override
	public int selectQueueNo(List<SingletonExecutor> selects, PbConvertParamConfig convertParamConfig) {
		return 0;
	}
}
