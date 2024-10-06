package com.lyc.common.pdfimage.utils;

/**
 * @description :
 *
 * @author : 		刘勇成
 * @date : 		2023/1/11 9:42
 *
 * @param
 * @return
 */
public class TimeUtils {

	// ms
	public static long countExpireTime(long delayMillis) {
		return System.currentTimeMillis() + delayMillis;
	}
}
