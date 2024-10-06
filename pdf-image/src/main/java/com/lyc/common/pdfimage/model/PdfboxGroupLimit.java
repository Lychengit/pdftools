package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.PdfBoxConvertorProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author : 		刘勇成
 * @description :
 *
 * 		转换组限制器
 *
 * @date : 		2023/9/15 11:23
 */
public class PdfboxGroupLimit {

	// 现有的占位者
	private final ConcurrentHashMap<CachePdfboxRendererGroup, String> TAKING_MAP = new ConcurrentHashMap<>();
	// 阻塞和唤醒承压能力之外的线程
	private final ReentrantLock TAKING_LOCK = new ReentrantLock(true);
	private final Condition WAIT_CONDITION = TAKING_LOCK.newCondition();

	public ConcurrentHashMap<CachePdfboxRendererGroup, String> getTakingMap() {
		return TAKING_MAP;
	}

	public ReentrantLock getTAKING_LOCK() {
		return TAKING_LOCK;
	}

	public Condition getWAIT_CONDITION() {
		return WAIT_CONDITION;
	}

	public boolean hasWaiters(final long timeout) throws InterruptedException {
		if (TAKING_LOCK.tryLock(timeout, TimeUnit.MILLISECONDS)) {
			try {
				return hasWaiters();
			} finally {
				TAKING_LOCK.unlock();
			}
		}
		return true;
	}

	public boolean hasWaiters() {
		return getTAKING_LOCK().hasWaiters(getWAIT_CONDITION());
	}

	/**
	 * 阻塞当前线程
 	 * @param group
	 * @param timeout
	 * @param groupMaxNum 转换组最大数量限制的大小
	 * @return
	 * @throws Throwable
	 */
	public boolean takingAndBlockThread(final CachePdfboxRendererGroup group, final long timeout, final long groupMaxNum) throws Throwable {
		if (group == null) {
			throw new NullPointerException();
		}
		TimeUnit unit = TimeUnit.MILLISECONDS;
		long nanos = unit.toNanos(timeout);
		if (TAKING_LOCK.tryLock(timeout, unit)) {
			try {
				// 如果获取到权限，则直接退出，否则阻塞等待获取占位资格或超时退出
				while (!taking(group, groupMaxNum)) {
					if (nanos <= 0) {
						return false;
					}
					// 没有就尝试唤醒一次
					PdfBoxConvertorProvider.WORKER.putRequest();
					// 进入阻塞状态，等待唤醒
					nanos = WAIT_CONDITION.awaitNanos(nanos);
				}
				return true;
			} finally {
				TAKING_LOCK.unlock();
			}
		}
		return false;
	}

	// 占位，占到位，则可以去转换
	private boolean taking(CachePdfboxRendererGroup group, long groupMaxNum) {
		// 如果已经占位的预览id，可允许去转换
		if (TAKING_MAP.containsKey(group)){
			return true;
		}
		// 如果位置还有空的，允许占位后去转换
		// 占位和绑定转换组
		if (TAKING_MAP.size() < groupMaxNum) {
			TAKING_MAP.put(group, "");
			return true;
		}
		return false;
	}

	// 尝试移除占位和唤醒阻塞的转换任务线程
	public boolean removeUnblockAllThreads(CachePdfboxRendererGroup group, long timeout) throws Throwable {
		if (TAKING_LOCK.tryLock(timeout, TimeUnit.MILLISECONDS)) {
			try {
				// 唤醒所有等待的线程
				if (removeTaking(group)) {
					if (hasWaiters()) {
						WAIT_CONDITION.signalAll();
					}
					return true;
				}
			} finally {
				TAKING_LOCK.unlock();
			}
		}
		return false;
	}

	// 移除占位标识
	private boolean removeTaking(CachePdfboxRendererGroup group) {
		// 移除占位和解绑转换组
		String remove = TAKING_MAP.remove(group);
		return remove != null;
	}

	// 尝试唤醒阻塞的转换任务线程
	public boolean unblockAllThreads(long timeout) throws Throwable {
		if (TAKING_LOCK.tryLock(timeout, TimeUnit.MILLISECONDS)) {
			try {
				// 唤醒所有等待的线程
				if (hasWaiters()) {
					WAIT_CONDITION.signalAll();
					return true;
				}
			} finally {
				TAKING_LOCK.unlock();
			}
		}
		return false;
	}
}
