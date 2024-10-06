package com.lyc.common.pdfimage.utils;

import com.lyc.common.pdfimage.exception.PdfConvertException;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @description :
 * 基于页宽高动态计算清晰度：
 *
 * 1、正常范围的宽高：默认返回标准清晰度，如2。
 *
 * 2、异常可控范围的宽高：
 *
 * 		1、大于标准宽高，小于最大异常宽高标准的：返回首位的清晰度startScale，默认是1。
 * 		2、大于	最大异常宽高标准的：
 * 		逻辑是基于跳表结构记录不同宽*高的规格 对应 不同清晰度scale（映射公式：宽*高/scale的平方  -> scale）。
 * 		在基于指定宽高来查询对应清晰度时，会类似一致性hash算法，会将往大的节点的清晰度返回。
 *
 * 3、超出可控范围的宽高：清晰度的计算公式：(最大允许的宽*高)/(当前页的宽*当前页的高)的平方根 = scale
 *
 * 默认值的范围：
 * 宽*高 < 1000*1000，返回标准清晰度2；
 * 宽*高 > 1350 * 1350, 返回清晰度[2,0)；
 *
 * @author : 		刘勇成
 * @date : 		2023/1/6 13:50
 *
 * @param
 * @return
 */
public class ImageScaleScalableUtil {
	protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(ImageScaleScalableUtil.class);

//	public static float STANDARD_SCALE = 2.0f;
//	public static int BASE_SIZE_MIN = 1000 * 1000;
//	public static int BASE_SIZE_MAX = 2700 * 2700;
//	public static String START_SCALE = "2";
//	public static String DECREASE_VALUE = "0.025";
//	public static int SCALE_NUM = 80;

//	public static ImageScaleSelector scaleSelector = new ImageScaleSelector("2", "0.025", 80, 2700 * 2700);

	public static float scale(float width, float height, float standardScale,
							  int baseSizeMin, int baseSizeMax, ImageScaleSelector scaleSelector) {

		int widthPx = (int) Math.max(Math.floor(width), 1);
		int heightPx = (int) Math.max(Math.floor(height), 1);

		long size = (long) widthPx * (long) heightPx;
		if (size > Integer.MAX_VALUE) {
			logger.error("宽*高不能大于Integer.MAX_VALUE，当前文件的页面尺寸超出范围，系统不支持。");
			throw new PdfConvertException("当前文件的页面尺寸超出范围，系统不支持。");
		}

		if(size <= baseSizeMin){
			return standardScale;
		}

		Float aFloat = scaleSelector.selectForKey((int) size);
		if (aFloat != null){
			return aFloat;
		}
		return (float) Math.sqrt(baseSizeMax/(width*height));
		
	}

	public static final class ImageScaleSelector {

		protected static final org.slf4j.Logger log = LoggerFactory.getLogger(ImageScaleSelector.class);

		// 异常宽高首位清晰度的大小
		private volatile String startScale = "2";
		// 异常宽高递减的清晰度的差值，如第二位清晰度 为 scale2 = startScale - decreaseValue，第三位清晰度为 scale3 = scale2 - decreaseValue
		// 以此依次递减
		private volatile String decreaseValue = "0.025";
		// 异常宽高基于差值递减的次数
		private volatile int scaleNum = 80;
		// 异常宽高的上限值
		private volatile int baseSizeMax = 2700 * 2700;

		private ConcurrentSkipListMap<Integer, Float> virtualNodes;

		public ImageScaleSelector(String startScale, String decreaseValue, int scaleNum,
								  int baseSizeMax) {
			this.startScale = startScale;
			this.decreaseValue = decreaseValue;
			this.scaleNum = scaleNum;
			this.baseSizeMax = baseSizeMax;
			rebuildScaleSelector();
		}

		public void rebuildScaleSelector(){
			this.virtualNodes = buildScaleSelector(this.startScale, this.decreaseValue, this.scaleNum, this.baseSizeMax);
		}

		private ConcurrentSkipListMap<Integer, Float> buildScaleSelector(String startScale, String decreaseValue, int scaleNum,
																		 int baseSizeMax) {
			ParameterFormatUtils.checkFloat(startScale);
			ParameterFormatUtils.checkFloat(decreaseValue);

			BigDecimal startScaleDecimal = new BigDecimal(startScale);
			BigDecimal decreaseValueDecimal = new BigDecimal(decreaseValue);

			log.info("开始构建scale选择器！");
			ConcurrentSkipListMap<Integer, Float>  virtualNodes = new ConcurrentSkipListMap<Integer, Float>();
			while (startScaleDecimal.compareTo(BigDecimal.ZERO) > 0 && startScaleDecimal.compareTo(decreaseValueDecimal) >= 0) {
				float size = baseSizeMax / startScaleDecimal.pow(2).floatValue();
				virtualNodes.put((int) size, startScaleDecimal.floatValue());
				startScaleDecimal = startScaleDecimal.subtract(decreaseValueDecimal);
			}

			log.info("构建scale选择器生效："+ virtualNodes.toString());
			return virtualNodes;
		}

		private Float selectForKey(Integer key) {
			Float invoker;
			if (!virtualNodes.containsKey(key)) {
				SortedMap<Integer, Float> tailMap = virtualNodes.tailMap(key);
				if (tailMap.isEmpty()) {
					return null;
				} else {
					key = tailMap.firstKey();
				}
			}
			invoker = virtualNodes.get(key);
			return invoker;
		}

		public String getStartScale() {
			return startScale;
		}

		public void setStartScale(String startScale) {
			this.startScale = startScale;
		}

		public String getDecreaseValue() {
			return decreaseValue;
		}

		public void setDecreaseValue(String decreaseValue) {
			this.decreaseValue = decreaseValue;
		}

		public int getScaleNum() {
			return scaleNum;
		}

		public void setScaleNum(int scaleNum) {
			this.scaleNum = scaleNum;
		}

		public int getBaseSizeMax() {
			return baseSizeMax;
		}

		public void setBaseSizeMax(int baseSizeMax) {
			this.baseSizeMax = baseSizeMax;
		}
	}

	public static final class ImageScaleModel {
		// 标准清晰度scale。
		private volatile float standardScale;
		// 标准宽高的上限值（小于下限值的为标准宽高，使用标准清晰度转换）
		private volatile int baseSizeMin;
		// 异常宽高的上限值（也可表示为每页转换时最大允许的内存占用大小，可按照int类型的计算，如，baseSizeMax = 2700 * 2700，每页转换占用的内存最大为2700 * 2700 * 4 / 1024 /1024 = 27M。
		// 动态scale的计算公式：宽*高*scale*scale == baseSizeMax）
		private volatile int baseSizeMax;
		private volatile ImageScaleSelector scaleSelector;

		public ImageScaleModel(float standardScale, int baseSizeMin, int baseSizeMax, ImageScaleSelector scaleSelector) {
			this.standardScale = standardScale;
			this.baseSizeMin = baseSizeMin;
			this.baseSizeMax = baseSizeMax;
			this.scaleSelector = scaleSelector;
		}

		public float getStandardScale() {
			return standardScale;
		}

		public void setStandardScale(float standardScale) {
			this.standardScale = standardScale;
		}

		public int getBaseSizeMin() {
			return baseSizeMin;
		}

		public void setBaseSizeMin(int baseSizeMin) {
			this.baseSizeMin = baseSizeMin;
		}

		public int getBaseSizeMax() {
			return baseSizeMax;
		}

		public void setBaseSizeMax(int baseSizeMax) {
			this.baseSizeMax = baseSizeMax;
		}

		public ImageScaleSelector getScaleSelector() {
			return scaleSelector;
		}

		public void setScaleSelector(ImageScaleSelector scaleSelector) {
			this.scaleSelector = scaleSelector;
		}
	}
}
