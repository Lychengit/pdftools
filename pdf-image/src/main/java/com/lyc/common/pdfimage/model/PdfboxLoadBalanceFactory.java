package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.service.PdfboxAbstractLoadBalance;
import com.lyc.common.pdfimage.service.impl.PbWeightedLeastTaskRule;
import com.lyc.common.pdfimage.service.impl.PbWeightedMostSkipTaskRule;
import com.lyc.common.pdfimage.service.impl.PbWeightedRoundRobinRule;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : 		刘勇成
 * @description : pdfbox转换相关负载算法工厂
 * @date : 		2023/4/4 11:33
 */
public class PdfboxLoadBalanceFactory {

	/**
	 * 策略集合map
	 */
	private static final Map<String, PdfboxAbstractLoadBalance> map = initMap();

	/**
	 * 基于枚举类初始化策略集合
	 * @return
	 */
	public static Map<String, PdfboxAbstractLoadBalance> initMap(){
		return Arrays.stream(EnumPdfboxLbModel.values()).collect(Collectors.toMap(EnumPdfboxLbModel::getValue, EnumPdfboxLbModel::getPdfboxAbstractLoadBalance, (k1, k2)->k1));
	}

	/**
	 * 基于key值获取相关策略
	 * @param ruleModel
	 * @return
	 */
	public static PdfboxAbstractLoadBalance buildRule(String ruleModel){
		if (map.containsKey(ruleModel)){
			PdfboxAbstractLoadBalance pdfboxAbstractLoadBalance = map.get(ruleModel);
			if (pdfboxAbstractLoadBalance != null) {
				return pdfboxAbstractLoadBalance;
			}
		}

		return PdfboxLoadBalanceObj.pbWeightedMostSkipTaskRule;
	}

	/**
	 * 获取策略集合
	 * @return
	 */
	public static Map<String, PdfboxAbstractLoadBalance> getLoadBalanceMap(){
		return map;
	}

	/**
	 * 自定义其他策略
	 * @param ruleModel
	 * @param loadBalance
	 */
	public static void putLoadBalance(String ruleModel, PdfboxAbstractLoadBalance loadBalance){
		map.put(ruleModel, loadBalance);
	}
}
