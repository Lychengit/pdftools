package com.lyc.common.pdfimage.model;

/**
 * @author : 		刘勇成
 * @description : 转换为pdf相关传参实例
 * @date : 		2023/3/4 10:52
 */
public class ToPdfTask<T> {

	public ToPdfTask() {
	}

    public ToPdfTask(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    // 是否做pdf格式校验
	// OPT_CHECK("1", "打开pdf格式校验开关"),
	// OPT_NO_CHECK("0", "关闭pdf格式校验开关"),
	private String toCheckPdf;

	// 是否部分解析和校验pdf（传入1开启部分解析消耗内存小，速度快，特别是大文件、pdf的obj对象较多的文件，效果很明显，
	// 但是不能检测pdf非主体的内容相关的解析错误问题，如需解析页内容是否错误的，还需传入0）
	// OPT_PARTIAL_PAGES("2", "开启部分解析+解析所有页的模式"),
	// OPT_PARTIAL("1", "开启部分解析的模式"),
	// OPT_FULL("0", "开启全量解析的模式"),
	private String pdfReaderPartial;

	// 是否做编辑加密校验（默认为true，也就是会检查是否有编辑相关的权限）
	private Boolean toEncryptPermissionsCheck = true;

	// 只做是否有编辑加密的校验（默认为false，也就是不只会检查是否有编辑加密的权限，兼容PRIV-2502）
	private Boolean toOnlyEncryptCheck = false;

	// 带有adobeSigned信息的pdf是否做加密校验（默认为false，也就是会做加密校验，兼容PRIV-3366）
	private Boolean toAdobeSignedCheck = false;

    private String ownerPassword;

	public String getToCheckPdf() {
		return toCheckPdf;
	}

	public void setToCheckPdf(String toCheckPdf) {
		this.toCheckPdf = toCheckPdf;
	}

	public String getPdfReaderPartial() {
		return pdfReaderPartial;
	}

	public void setPdfReaderPartial(String pdfReaderPartial) {
		this.pdfReaderPartial = pdfReaderPartial;
	}

	public Boolean getToEncryptPermissionsCheck() {
		return toEncryptPermissionsCheck;
	}

	public void setToEncryptPermissionsCheck(Boolean toEncryptPermissionsCheck) {
		this.toEncryptPermissionsCheck = toEncryptPermissionsCheck;
	}

	public Boolean getToOnlyEncryptCheck() {
		return toOnlyEncryptCheck;
	}

	public void setToOnlyEncryptCheck(Boolean toOnlyEncryptCheck) {
		this.toOnlyEncryptCheck = toOnlyEncryptCheck;
	}

	public Boolean getToAdobeSignedCheck() {
		return toAdobeSignedCheck;
	}

	public void setToAdobeSignedCheck(Boolean toAdobeSignedCheck) {
		this.toAdobeSignedCheck = toAdobeSignedCheck;
	}

	public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }
}
