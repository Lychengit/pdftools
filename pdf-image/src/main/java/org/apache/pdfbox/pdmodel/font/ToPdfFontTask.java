package org.apache.pdfbox.pdmodel.font;

/**
 * @author : 		刘勇成
 *
 * @description : 转换字体相关传参实例
 *
 * @date : 		2023/3/4 10:52
 */
public class ToPdfFontTask<T> {

	public ToPdfFontTask() {
	}


	// 用于记录当前pdfbox字体别名与真实字体的映射关系版本，如果传入的值为PDFBOX，会将映射集合内容重置为初始化状态。
	private String pdfboxFontSubstitutesStr;


	public String getPdfboxFontSubstitutesStr() {
		return pdfboxFontSubstitutesStr;
	}

	public void setPdfboxFontSubstitutesStr(String pdfboxFontSubstitutesStr) {
		this.pdfboxFontSubstitutesStr = pdfboxFontSubstitutesStr;
	}
}
