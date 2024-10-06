package com.lyc.common.pdfimage.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : 		刘勇成
 * @description : 负载均衡相关的工具类
 * @date : 		2023/3/29 14:41
 */
public class LBalanceUtils {

	/**
	 * 轮询算法方法
	 * @param nextServerCyclicCounter
	 * @param modulo
	 * @return
	 */
	public static int incrementAndGetModulo(AtomicInteger nextServerCyclicCounter, int modulo) {
		if (modulo == 0){
			return 0;
		}
		for (;;) {
			int current = nextServerCyclicCounter.get();
			int next = (current + 1) % modulo;
			if (nextServerCyclicCounter.compareAndSet(current, next)) {
				return Math.abs(next);
			}
		}
	}
}
