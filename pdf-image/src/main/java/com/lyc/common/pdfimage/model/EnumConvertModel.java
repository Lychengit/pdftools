package com.lyc.common.pdfimage.model;

/**
 * @author : 		刘勇成
 * @description : 转换的模式
 * @date : 		2023/3/3 17:50
 */
public enum EnumConvertModel {

	/**
	 * 转换的级别，用于分辨是首次转换，还是串行等待超时后去使用子转换实例做转换
	 */
	OPT_SINGLE("1", "单转换实例模式"),
	OPT_MORE("2", "多转换实例模式"),
	;

	EnumConvertModel(String value, String info) {
		this.value = value;
		this.info = info;
	}

	private String value;

	private String info;

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
