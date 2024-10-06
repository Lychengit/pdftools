package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.utils.weakmap.ReferenceMapEntry;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : 		刘勇成
 * @description :
 * @date : 		2024/6/17 19:12
 */
public class ReentrantLockEntry implements ReferenceMapEntry {

	private final ReentrantLock reentrantLock;

	// 局部强引用，避免转换组还没有过期就因fullgc导致转换组entry被回收
	private Map.Entry entryObj;

	public ReentrantLockEntry(boolean fair) {
		this.reentrantLock = new ReentrantLock(fair);
	}

	public ReentrantLock getReentrantLock() {
		return reentrantLock;
	}

	@Override
	public Map.Entry getEntry() {
		return entryObj;
	}

	@Override
	public void setEntry(Map.Entry entry) {
		this.entryObj = entry;
	}
}
