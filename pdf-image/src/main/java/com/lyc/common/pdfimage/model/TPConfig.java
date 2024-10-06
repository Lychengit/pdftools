package com.lyc.common.pdfimage.model;

import java.util.concurrent.TimeUnit;

/**
 * @author : 		刘勇成
 * @description : 线程池的配置类
 * @date : 		2023/3/22 15:26
 */
public class TPConfig {

	// 线程池配置threadPool -> tP
	private int tPCorePoolSize = Runtime.getRuntime().availableProcessors() * 2;
	private int tPMaximumPoolSize = Runtime.getRuntime().availableProcessors() * 2;
	private long tPKeepAliveTime = 60;
	private TimeUnit tPUnit = TimeUnit.MILLISECONDS;
	private int tPCapacity = 1000;
	// 线程池个数
	private int tPNum = Runtime.getRuntime().availableProcessors() * 2;

	public TPConfig(int tPCorePoolSize, int tPMaximumPoolSize, int tPCapacity) {
		this.tPCorePoolSize = tPCorePoolSize;
		this.tPMaximumPoolSize = tPMaximumPoolSize;
		this.tPCapacity = tPCapacity;
	}

	public TPConfig() {
	}

	public int gettPCorePoolSize() {
		return tPCorePoolSize;
	}

	public void settPCorePoolSize(int tPCorePoolSize) {
		this.tPCorePoolSize = Math.max(1, tPCorePoolSize);
	}

	public int gettPMaximumPoolSize() {
		return tPMaximumPoolSize;
	}

	public void settPMaximumPoolSize(int tPMaximumPoolSize) {
		this.tPMaximumPoolSize = tPMaximumPoolSize;
	}

	public long gettPKeepAliveTime() {
		return tPKeepAliveTime;
	}

	public void settPKeepAliveTime(long tPKeepAliveTime) {
		this.tPKeepAliveTime = tPKeepAliveTime;
	}

	public TimeUnit gettPUnit() {
		return tPUnit;
	}

	public void settPUnit(TimeUnit tPUnit) {
		this.tPUnit = tPUnit;
	}

	public int gettPCapacity() {
		return tPCapacity;
	}

	public void settPCapacity(int tPCapacity) {
		this.tPCapacity = Math.max(1, tPCapacity);
	}

	public int gettPNum() {
		return tPNum;
	}

	public void settPNum(int tPNum) {
		this.tPNum = tPNum;
	}
}
