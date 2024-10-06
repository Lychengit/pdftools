package com.lyc.common.pdfimage.model;

import java.util.concurrent.TimeUnit;

/**
 * @description :
 * 时间轮相关参数配置类
 * @author : 		刘勇成
 * @date : 		2023/3/2 16:23
 *
 * @param
 * @return
 */
public class WTConfig {

	// 时间轮配置hashedWheelTimer -> wT
	private int wTTimerNum = 1;
	private long wTTickDuration = 100;
	private TimeUnit wTUnit = TimeUnit.MILLISECONDS;
	private int wTticksPerWheel = 1024;
	private boolean wTleakDetection = false;
	// -1表示积压无上限
	private long wTmaxPendingTimeouts = 10000;

	public int getwTTimerNum() {
		return wTTimerNum;
	}

	public void setwTTimerNum(int wTTimerNum) {
		this.wTTimerNum = Math.max(1, wTTimerNum);
	}

	public long getwTTickDuration() {
		return wTTickDuration;
	}

	public void setwTTickDuration(long wTTickDuration) {
		this.wTTickDuration = wTTickDuration;
	}

	public TimeUnit getwTUnit() {
		return wTUnit;
	}

	public void setwTUnit(TimeUnit wTUnit) {
		this.wTUnit = wTUnit;
	}

	public int getwTticksPerWheel() {
		return wTticksPerWheel;
	}

	public void setwTticksPerWheel(int wTticksPerWheel) {
		this.wTticksPerWheel = wTticksPerWheel;
	}

	public boolean iswTleakDetection() {
		return wTleakDetection;
	}

	public void setwTleakDetection(boolean wTleakDetection) {
		this.wTleakDetection = wTleakDetection;
	}

	public long getwTmaxPendingTimeouts() {
		return wTmaxPendingTimeouts;
	}

	public void setwTmaxPendingTimeouts(long wTmaxPendingTimeouts) {
		this.wTmaxPendingTimeouts = Math.max(1, wTmaxPendingTimeouts);
	}
}
