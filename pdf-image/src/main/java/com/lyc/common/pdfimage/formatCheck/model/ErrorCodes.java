package com.lyc.common.pdfimage.formatCheck.model;

public enum ErrorCodes {

	PB_RENDER_ERR(30032032, "","当前页内部格式有异常，尝试使用adobe工具或浏览器的打印另存为功能修复pdf后，再尝试重新上传"),
	;

	private ErrorCodes(int code, String type, String desc) {
		this.code = code;
		this.type = type;
		this.desc = desc;
	}
	private int code;
	private String type;
	private String desc;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
