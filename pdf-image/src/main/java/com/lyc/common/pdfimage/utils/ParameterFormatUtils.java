package com.lyc.common.pdfimage.utils;

import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * @author : 		刘勇成
 * @description :
 * @date : 		2023/1/6 11:40
 */
public class ParameterFormatUtils {

	protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(ParameterFormatUtils.class);

	public static Float checkFloat(String value){
		try {
			if (value != null) {
				Float valueF = Float.parseFloat(value);
				if (valueF < 0 || valueF.isInfinite()) {
					throw new NumberFormatException();
				}
				return valueF;
			}

		} catch (Exception e){
			logger.error("值【"+value+"】检查发现异常！", e);
			if (e instanceof NumberFormatException){
				throw new RuntimeException("值【"+value+"】不符合要求，需要是浮点数字类型的，数值范围必须大于等于0或小于等于3.4028235e+38f.");
			}
			throw new RuntimeException("值【" + value + "】检查发现异常！");
		}
		return null;
	}

	public static Integer checkInt(String value){
		try {
			if (value != null) {
				BigDecimal valueB = new BigDecimal(value);
				if(valueB.compareTo(BigDecimal.ZERO) < 0 ||
						valueB.compareTo(new BigDecimal(String.valueOf(Integer.MAX_VALUE))) > 0) {
					throw new NumberFormatException();
				}
				return Integer.parseInt(value);
			}

		} catch (Exception e){
			logger.error("值【"+value+"】检查发现异常！", e);
			if (e instanceof NumberFormatException){
				throw new RuntimeException("值【"+value+"】不符合要求，需要是整形数字类型的，数值范围必须大于等于0或小于等于2147483647.");
			}
			throw new RuntimeException("值【" + value + "】检查发现异常！");
		}
		return null;
	}

	public static Long checkLong(String value){
		try {
			if (value != null) {
				BigDecimal valueB = new BigDecimal(value);
				if(valueB.compareTo(BigDecimal.ZERO) < 0 ||
						valueB.compareTo(new BigDecimal(String.valueOf(Long.MAX_VALUE))) > 0) {
					throw new NumberFormatException();
				}
				return Long.parseLong(value);
			}

		} catch (Exception e){
			logger.error("值【"+value+"】检查发现异常！", e);
			if (e instanceof NumberFormatException){
				throw new RuntimeException("值【"+value+"】不符合要求，需要是长整形数字类型的，数值范围必须大于等于0或小于等于9223372036854775807.");
			}
			throw new RuntimeException("值【" + value + "】检查发现异常！");
		}
		return null;
	}
}
