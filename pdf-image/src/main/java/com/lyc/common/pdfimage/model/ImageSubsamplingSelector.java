package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.utils.ParameterFormatUtils;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author : 		刘勇成
 * @description : 图片子采样的采样粒度粗细
 * @date : 		2023/4/19 11:22
 */
public class ImageSubsamplingSelector {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(ImageSubsamplingSelector.class);

	// 异常宽高首位图片子采样粒度的大小
	private volatile String startSubsampling = "1";
	// 异常宽高递增的图片子采样粒度的差值，如第二位图片子采样粒度 为 subsampling2 = startSubsampling + decreaseValue，第三位图片子采样粒度为 subsampling3 = subsampling2 + decreaseValue
	// 以此依次递增
	private volatile String decreaseValue = "1";
	// 异常宽高基于差值递增的次数
	private volatile int subsamplingNum = 8;
	// 异常宽高的上限值（也可表示为pdf某页每张图片转换时最大允许的内存占用大小（估值）。
	// 动态subsampling的计算公式：宽*高 / baseSizeMax == subsampling*subsampling）
	private volatile int baseSizeMax = 2700 * 2700;

	private ConcurrentSkipListMap<Integer, Integer> virtualNodes;

	public ImageSubsamplingSelector(String startSubsampling, String decreaseValue, int subsamplingNum,
							  int baseSizeMax) {
		this.startSubsampling = startSubsampling;
		this.decreaseValue = decreaseValue;
		this.subsamplingNum = subsamplingNum;
		this.baseSizeMax = baseSizeMax;
		rebuildSubsamplingSelector();
	}

	public ImageSubsamplingSelector() {
		rebuildSubsamplingSelector();
	}

	public void rebuildSubsamplingSelector(){
		this.virtualNodes = buildSubsamplingSelector(this.startSubsampling, this.decreaseValue, this.subsamplingNum, this.baseSizeMax);
	}

	private ConcurrentSkipListMap<Integer, Integer> buildSubsamplingSelector(String startSubsampling, String decreaseValue, int subsamplingNum,
																	 int baseSizeMax) {
		ParameterFormatUtils.checkInt(startSubsampling);
		ParameterFormatUtils.checkInt(decreaseValue);

		BigDecimal startSubsamplingDecimal = new BigDecimal(startSubsampling);
		BigDecimal decreaseValueDecimal = new BigDecimal(decreaseValue);

		log.info("开始构建subsampling选择器！");
		ConcurrentSkipListMap<Integer, Integer>  virtualNodes = new ConcurrentSkipListMap<Integer, Integer>();
		while (subsamplingNum > 0) {
			int size = baseSizeMax * startSubsamplingDecimal.pow(2).intValue();
			virtualNodes.put(size, startSubsamplingDecimal.intValue());
			startSubsamplingDecimal = startSubsamplingDecimal.add(decreaseValueDecimal);
			subsamplingNum--;
		}

		log.info("构建subsampling选择器生效："+ virtualNodes.toString());
		return virtualNodes;
	}

	public Integer selectForKey(Integer key) {
		Integer invoker;
		if (!virtualNodes.containsKey(key)) {
			SortedMap<Integer, Integer> tailMap = virtualNodes.tailMap(key);
			if (tailMap.isEmpty()) {
				return null;
			} else {
				key = tailMap.firstKey();
			}
		}
		invoker = virtualNodes.get(key);
		return invoker;
	}

	public String getStartSubsampling() {
		return startSubsampling;
	}

	public void setStartSubsampling(String startSubsampling) {
		this.startSubsampling = startSubsampling;
	}

	public String getDecreaseValue() {
		return decreaseValue;
	}

	public void setDecreaseValue(String decreaseValue) {
		this.decreaseValue = decreaseValue;
	}

	public int getSubsamplingNum() {
		return subsamplingNum;
	}

	public void setSubsamplingNum(int subsamplingNum) {
		this.subsamplingNum = subsamplingNum;
	}

	public int getBaseSizeMax() {
		return baseSizeMax;
	}

	public void setBaseSizeMax(int baseSizeMax) {
		this.baseSizeMax = baseSizeMax;
	}
}
