package com.lyc.common.pdfimage.model;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @description : 状态选项对象
 *
 * @author : 		刘勇成
 * @date : 		2023/2/27 17:51
 *
 * @param
 * @return
 */
public class OptRequest {

	private static final int OPT_INIT = 0;
	private static final int OPT_CANCELLED = 1;
	private static final AtomicIntegerFieldUpdater<OptRequest> STATE_UPDATER =
			AtomicIntegerFieldUpdater.newUpdater(OptRequest.class, "state");

	private volatile int state = OPT_INIT;

	public boolean compareAndSetState(int expected, int state) {
		return STATE_UPDATER.compareAndSet(this, expected, state);
	}

	public boolean cancel() {
		return compareAndSetState(OPT_INIT, OPT_CANCELLED);
	}

	public int state() {
		return state;
	}

	public boolean isCancelled() {
		return state() == OPT_CANCELLED;
	}
}
