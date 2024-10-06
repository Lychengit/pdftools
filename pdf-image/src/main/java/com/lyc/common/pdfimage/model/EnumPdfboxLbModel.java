package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.service.PdfboxAbstractLoadBalance;

/**
 * @author : 		刘勇成
 * @description : pdfbox多转换实例负载模式
 * @date : 		2023/3/3 17:50
 */
public enum EnumPdfboxLbModel {

	/**
	 *
	 * pdfbox多转换实例负载模式
	 *
	 *
	 */
	OPT_WEIGHTED_ROUND_ROBIN(PdfboxLoadBalanceObj.pbWeightedRoundRobinRule,"1", "加权轮询算法"),
	OPT_WEIGHTED_LEAST_TASK(PdfboxLoadBalanceObj.pbWeightedLeastTaskRule,"2", "加权最小转换任务数算法"),
	OPT_WEIGHTED_MOST_SKIP_TASK(PdfboxLoadBalanceObj.pbWeightedMostSkipTaskRule,"3", "加权最大转换任务数跳过算法"),
	;

	EnumPdfboxLbModel(PdfboxAbstractLoadBalance pdfboxAbstractLoadBalance, String value, String info) {
		this.pdfboxAbstractLoadBalance = pdfboxAbstractLoadBalance;
		this.value = value;
		this.info = info;
	}

	private String value;

	private String info;

	private PdfboxAbstractLoadBalance pdfboxAbstractLoadBalance;

	public PdfboxAbstractLoadBalance getPdfboxAbstractLoadBalance() {
		return pdfboxAbstractLoadBalance;
	}

	public void setPdfboxAbstractLoadBalance(PdfboxAbstractLoadBalance pdfboxAbstractLoadBalance) {
		this.pdfboxAbstractLoadBalance = pdfboxAbstractLoadBalance;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}



}
