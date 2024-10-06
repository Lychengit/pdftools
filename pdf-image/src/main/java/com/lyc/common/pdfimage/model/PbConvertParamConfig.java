package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.utils.FileUtils;

import java.io.File;

/**
 * @author : 		刘勇成
 * @description : pdfbox转换相关参数
 * @date : 		2022/12/14 14:03
 */
public class PbConvertParamConfig {
	// pdfbox load加载的pddocument实例的缓存时间（ms）
	private volatile long cachePdfrendererTimeout = 30000;
	// 转换组里面的转换实例的缓存时间（注意，一般小于等于cachePdfrendererTimeout）。
	private volatile long groupMapContinueTime = 8000;
	// pdfbox子转换实例的最大个数
	private volatile long pdDocumentMaxSize = 1;
	// pdfbox转换超时时间（ms）
	private volatile long convertTimeout = 30000;
	// pdf转出的图片宽高的上限值，越高转换时内存占用越大。
	private volatile float maxWidthHeight = 2700;

	// pdfbox转换超时重新负载最大次数
	private volatile int waitTimeoutLBMaxSize = 10;
	// pdfbox串行预览等待转换的超时时间（ms）
	private volatile long convertWaitTimeout = 9000;
	// pdfbox单转换实例情况下，转换超时时间（ms）（convertWaitTimeout* waitTimeoutLBMaxSize）
	private volatile long singleConvertTimeout = convertWaitTimeout* waitTimeoutLBMaxSize;

	// pdfbox串行添加初始化转换实例的超时时间（ms）
	private volatile long rootTaskNewTimeout = 30000;

	// pdfbox线程池串行预览等待转换的超时时间（ms）
	private volatile long convertTpWaitTimeout = 300000;

	// 当pdf大于当前阈值时，则使用单转换实例转换（字节）
	private volatile long singleFileMaxSize = 200 * 1024 * 1024;
	// 当pdf的页数大于当前阈值时，则使用单转换实例转换（页）
	private volatile long singleFileMaxCount = 10000;
	// 当pdf obj对象数大于当前阈值时，则使用单转换实例转换（个）（0表示不开启转换实例组获取预知pdf对象数）
	private volatile long singleFileMaxPdfObj = 0;

	// 使用最小负载策略时最小任务数的下限值（个）（涉及获取单线程线程池执行器队列号的负载均衡策略）
	private int singletonLBMinfloor = 20;
	// 使用最小负载策略时最大任务数的下限值（个）（涉及获取单线程线程池执行器队列号的负载均衡策略）
	private int singletonLBMaxfloor = 30;

	// pdfbox转换任务负载模式（将某个预览id的任务负载到多转换实例）
	private volatile int convertTaskLbRule = Integer.parseInt(EnumPdfboxLbModel.OPT_WEIGHTED_MOST_SKIP_TASK.getValue());
	// pdfbox转换实例负载模式（将某个初始化的转换实例负载到转换队列）
	private volatile int convertLbRule = Integer.parseInt(EnumPdfboxLbModel.OPT_WEIGHTED_MOST_SKIP_TASK.getValue());

	// 内联图片临时文件处理方式的开关（默认关闭）
	private volatile int inlineImageTmpSwitch = 0;
	// 内联图片多大触发基于临时文件的方式来解码图片数据（主要针对类似 游程编码算法的反解压操作）,the inline format gives the reader less flexibility in managing the image data, it shall be used only for small images (4 KB or less).
	private volatile int inlineImageTmpMaxSize = 4 * 1024;

	protected static final String INLINE_IMAGE_TMP_DICTIONARY = FileUtils.getTempDir("pbInlineImageTmpFiles");
	static {
		File file = new File(INLINE_IMAGE_TMP_DICTIONARY);
		if (!file.exists() || !file.isDirectory()){
			file.mkdirs();
		}
	}
	// 内联图片临时文件存储目录（如果pdfbox设置了临时文件存储路径，则首选转换实例的临时目录）
	private volatile String inlineImageTmpDirectory = INLINE_IMAGE_TMP_DICTIONARY;

	// 转换实例最大转换次数，因pdfbox3.0是增量的，转换的页次越多，将导致转换实例越来越大，为了避免这个问题，需在转换一定页次后就释放旧的转换实例
	private volatile long rendererMaxCount = 500;

	// 最大转换组限制的数量
	private volatile long groupMaxNum = 100;

	public long getCachePdfrendererTimeout() {
		return cachePdfrendererTimeout;
	}

	public void setCachePdfrendererTimeout(long cachePdfrendererTimeout) {
		this.cachePdfrendererTimeout = cachePdfrendererTimeout;
	}

	public long getGroupMapContinueTime() {
		return groupMapContinueTime;
	}

	public void setGroupMapContinueTime(long groupMapContinueTime) {
		this.groupMapContinueTime = groupMapContinueTime;
	}

	public long getPdDocumentMaxSize() {
		return pdDocumentMaxSize;
	}

	public void setPdDocumentMaxSize(long pdDocumentMaxSize) {
		this.pdDocumentMaxSize = pdDocumentMaxSize;
	}

	public long getConvertTimeout() {
		return convertTimeout;
	}

	public void setConvertTimeout(long convertTimeout) {
		this.convertTimeout = convertTimeout;
	}

	public float getMaxWidthHeight() {
		return maxWidthHeight;
	}

	public void setMaxWidthHeight(float maxWidthHeight) {
		this.maxWidthHeight = maxWidthHeight;
	}

	public int getWaitTimeoutLBMaxSize() {
		return waitTimeoutLBMaxSize;
	}

	public void setWaitTimeoutLBMaxSize(int waitTimeoutLBMaxSize) {
		this.waitTimeoutLBMaxSize = waitTimeoutLBMaxSize;
	}

	public long getConvertWaitTimeout() {
		return convertWaitTimeout;
	}

	public void setConvertWaitTimeout(long convertWaitTimeout) {
		this.convertWaitTimeout = convertWaitTimeout;
	}

	public long getSingleConvertTimeout() {
		return singleConvertTimeout;
	}

	public void setSingleConvertTimeout(long singleConvertTimeout) {
		this.singleConvertTimeout = singleConvertTimeout;
	}

	public long getRootTaskNewTimeout() {
		return rootTaskNewTimeout;
	}

	public void setRootTaskNewTimeout(long rootTaskNewTimeout) {
		this.rootTaskNewTimeout = rootTaskNewTimeout;
	}

	public long getConvertTpWaitTimeout() {
		return convertTpWaitTimeout;
	}

	public void setConvertTpWaitTimeout(long convertTpWaitTimeout) {
		this.convertTpWaitTimeout = convertTpWaitTimeout;
	}

	public long getSingleFileMaxSize() {
		return singleFileMaxSize;
	}

	public void setSingleFileMaxSize(long singleFileMaxSize) {
		this.singleFileMaxSize = singleFileMaxSize;
	}

	public long getSingleFileMaxCount() {
		return singleFileMaxCount;
	}

	public void setSingleFileMaxCount(long singleFileMaxCount) {
		this.singleFileMaxCount = singleFileMaxCount;
	}

	public long getSingleFileMaxPdfObj() {
		return singleFileMaxPdfObj;
	}

	public void setSingleFileMaxPdfObj(long singleFileMaxPdfObj) {
		this.singleFileMaxPdfObj = singleFileMaxPdfObj;
	}

	public int getSingletonLBMinfloor() {
		return singletonLBMinfloor;
	}

	public void setSingletonLBMinfloor(int singletonLBMinfloor) {
		this.singletonLBMinfloor = singletonLBMinfloor;
	}

	public int getSingletonLBMaxfloor() {
		return singletonLBMaxfloor;
	}

	public void setSingletonLBMaxfloor(int singletonLBMaxfloor) {
		this.singletonLBMaxfloor = singletonLBMaxfloor;
	}

	public int getConvertTaskLbRule() {
		return convertTaskLbRule;
	}

	public void setConvertTaskLbRule(int convertTaskLbRule) {
		this.convertTaskLbRule = convertTaskLbRule;
	}

	public int getConvertLbRule() {
		return convertLbRule;
	}

	public void setConvertLbRule(int convertLbRule) {
		this.convertLbRule = convertLbRule;
	}

	public int getInlineImageTmpSwitch() {
		return inlineImageTmpSwitch;
	}

	public void setInlineImageTmpSwitch(int inlineImageTmpSwitch) {
		this.inlineImageTmpSwitch = inlineImageTmpSwitch;
	}

	public int getInlineImageTmpMaxSize() {
		return inlineImageTmpMaxSize;
	}

	public void setInlineImageTmpMaxSize(int inlineImageTmpMaxSize) {
		this.inlineImageTmpMaxSize = inlineImageTmpMaxSize;
	}

	public static String getInlineImageTmpDictionary() {
		return INLINE_IMAGE_TMP_DICTIONARY;
	}

	public String getInlineImageTmpDirectory() {
		return inlineImageTmpDirectory;
	}

	public void setInlineImageTmpDirectory(String inlineImageTmpDirectory) {
		this.inlineImageTmpDirectory = inlineImageTmpDirectory;
	}

	public long getRendererMaxCount() {
		return rendererMaxCount;
	}

	public void setRendererMaxCount(long rendererMaxCount) {
		this.rendererMaxCount = rendererMaxCount;
	}

	public long getGroupMaxNum() {
		return groupMaxNum;
	}

	public void setGroupMaxNum(long groupMaxNum) {
		this.groupMaxNum = groupMaxNum;
	}
}
