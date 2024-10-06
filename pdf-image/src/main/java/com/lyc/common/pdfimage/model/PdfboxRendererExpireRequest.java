package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.PdfBoxConvertorProvider;
import com.lyc.common.pdfimage.utils.TimeUtils;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author : 		刘勇成
 * @description : Pdfbox转换实例过期监控请求
 * @date : 		2022/12/13 10:51
 */
public class PdfboxRendererExpireRequest extends MonitorRequest<CachePdfboxRendererGroup> {

	private static final Logger log = LoggerFactory.getLogger(PdfboxRendererExpireRequest.class);

	// 转换组里面的转换实例的延时时长
	private final long groupMapContinueTime;
	// 转换组里面的转换实例的过期时间
	private volatile long groupMapExpireTime;

	public PdfboxRendererExpireRequest(CachePdfboxRendererGroup body, long internalLockLeaseTime, long groupMapContinueTime) {
		super(body, internalLockLeaseTime);
		this.groupMapContinueTime = groupMapContinueTime;
		continueExpireTime();
	}

	public long getGroupMapContinueTime() {
		return groupMapContinueTime;
	}

	public long getGroupMapExpireTime() {
		return groupMapExpireTime;
	}

	public void setGroupMapExpireTime(long groupMapExpireTime) {
		this.groupMapExpireTime = groupMapExpireTime;
	}

	@Override
	public CompletableFuture<MonitorResponse<CachePdfboxRendererGroup>> submitRequest() throws Throwable {
		monitor();
		return this.future();
	}

	@Override
	public void continueExpireTime() {
		super.continueExpireTime();
		continueGroupMapExpireTime();
	}

	public void continueGroupMapExpireTime(){
		setGroupMapExpireTime(TimeUtils.countExpireTime(getGroupMapContinueTime()));
	}

	public boolean isGroupMapExpireTime(){
		return this.getGroupMapExpireTime() < System.currentTimeMillis();
	}

	@Override
	public boolean toCancel() throws Throwable {
		return false;
	}

	@Override
	public boolean toCheck() throws Throwable {
		return false;
	}

	private void monitor(){
		PdfboxRendererExpireRequest request = this;
		CachePdfboxRendererGroup body = request.getBody();
		if (body == null){
			throw new RuntimeException("PdfboxRendererExpireRequest的body为null");
		}
		HashedWheelTimer hashedWheelTimer = body.getHashedWheelTimer();
		if (hashedWheelTimer == null){
			throw new RuntimeException("CachePdfboxRendererGroup的延时监控器不能为null");
		}
		ReentrantReadWriteLock readWriteLock = body.getReadWriteLock();
		if (readWriteLock == null){
			throw new RuntimeException("CachePdfboxRendererGroup的readWriteLock为null");
		}
		File file = body.getFile();
		if (file == null){
			throw new RuntimeException("CachePdfboxRendererGroup的file为null");
		}

		hashedWheelTimer.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {

				if (body.hasTask()) {
					request.continueExpireTime();
				} else {
					if (request.isGroupMapExpireTime()) {
						PdfBoxConvertorProvider.clearGroupMap(body);
					}
					// 1、没有预览任务：当延时任务不存在等锁的线程，锁为非锁状态。
					// 2、缓存pdfbox文件一段时间：过期。
					// 满足上述两点则删除当前pdf文件，以及结束监听
					if (request.isExpireTime()) {
						try {
							if (PdfBoxConvertorProvider.clearGroup(body)) {
								return;
							}
						} catch (Throwable e) {
							log.error("Pdfbox清除转换实例组异常~", e);
						}
					}
				}

				monitor();
			}
		}, (request.getGroupMapContinueTime() / 3) + 1, TimeUnit.MILLISECONDS);
	}

}
