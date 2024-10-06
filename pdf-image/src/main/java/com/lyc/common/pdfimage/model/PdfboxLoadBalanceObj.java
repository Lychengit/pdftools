package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.service.PdfboxAbstractLoadBalance;
import com.lyc.common.pdfimage.service.impl.PbWeightedLeastTaskRule;
import com.lyc.common.pdfimage.service.impl.PbWeightedMostSkipTaskRule;
import com.lyc.common.pdfimage.service.impl.PbWeightedRoundRobinRule;

/**
 * @author : 		刘勇成
 * @description : pdfbox转换相关负载算法实例的容器
 * @date : 		2023/4/4 11:33
 */
public class PdfboxLoadBalanceObj {

	/**
	 * 策略
	 */
	public static final PdfboxAbstractLoadBalance pbWeightedRoundRobinRule = new PbWeightedRoundRobinRule();
	public static final PdfboxAbstractLoadBalance pbWeightedLeastTaskRule = new PbWeightedLeastTaskRule();
	public static final PdfboxAbstractLoadBalance pbWeightedMostSkipTaskRule = new PbWeightedMostSkipTaskRule();
}
