package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.PdfBoxConvertorProvider;
import org.apache.pdfbox.contentstream.operator.graphics.BeginInlineImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;

import java.awt.geom.AffineTransform;
import java.io.IOException;

/**
 * @author : 		刘勇成
 * @description : 构建自己的画图工具（如处理pdf里面的图片子采样的粒度）
 * @date : 		2023/4/18 17:40
 */
public class PdfboxPageDrawer extends PageDrawer {

	private final String scratchFileDirectory;

	/**
	 * Constructor.
	 *
	 * @param parameters Parameters for page drawing.
	 * @throws IOException If there is an error loading properties from the file.
	 */
	public PdfboxPageDrawer(PageDrawerParameters parameters, String previewid) throws IOException {
		super(parameters);
		PbConvertParamConfig convertParamConfig = PdfBoxConvertorProvider.CONVERT_PARAM_CONFIG;
		// 内联图片自定义操作器的开关
		int inlineImageTmpSwitch = convertParamConfig.getInlineImageTmpSwitch();
		this.scratchFileDirectory = convertParamConfig.getInlineImageTmpDirectory();
		if (inlineImageTmpSwitch != 0) {
			// 使用自定义的默认目录来存储自己的临时文件

			// 添加自己的内联图片处理操作器。因pdfbox涉及到内联图片里面的游程编码算法时，
			// 解压出来的内联图片可能很大（10M解压出来可能360M），比较费内存，这里转移到
			// 临时文件来处理以此减少内存占用，避免内存溢出。
			BeginInlineImage beginInlineImage = new BeginInlineImage(this);
			addOperator(new PdfboxBeginInlineImage(this, beginInlineImage, this.scratchFileDirectory, previewid));
		}
	}

	/**
	 * 自定义处理pdf里面图片子采样的粒度
	 * @param pdImage
	 * @param at
	 * @return
	 */
	@Override
	protected int getSubsampling(PDImage pdImage, AffineTransform at) {
		int pdImageWidth = Math.max(pdImage.getWidth(), 1);
		int pdImageHeight = Math.max(pdImage.getHeight(), 1);
		long size = (long) pdImageWidth * (long) pdImageHeight;
		if (size > Integer.MAX_VALUE) {
			throw new RuntimeException("图片子采样时宽*高不能大于Integer.MAX_VALUE");
		}
		ImageSubsamplingSelector subsamplingSelector = PdfBoxConvertorProvider.IMAGE_SUBSAMPLING_SELECTOR;
		Integer subsampling = subsamplingSelector.selectForKey((int) size);
		if (subsampling != null){
			return subsampling;
		}
		return super.getSubsampling(pdImage, at);
	}

	public String getScratchFileDirectory() {
		return scratchFileDirectory;
	}
}
