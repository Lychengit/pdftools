package com.lyc.common.pdfimage.model.thread;

import org.slf4j.LoggerFactory;

/**
 * @author : 		刘勇成
 * @description : 长轮询任务（每10s广播一次，可调用putRequest()提前唤醒轮询线程，可用于实现自定义参数的变动监控）
 * @date : 		2022/11/21 17:37
 */
public abstract class MonitorLongPollingRunnable extends ServiceThread {

	protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(MonitorLongPollingRunnable.class);

	// ms
	private final long waitForRunningTime;

	private final String serviceName;

	public MonitorLongPollingRunnable(long waitForRunningTime, String serviceName) {
		this.waitForRunningTime = waitForRunningTime;
		this.serviceName = serviceName;
	}

	public synchronized void putRequest() {
		this.wakeup();
	}

	@Override
	public void run() {
		log.info(this.getServiceName() + " 服务开始启动");

		while (!this.isStopped()) {
			try {
				this.waitForRunning(waitForRunningTime);
				this.doCommit();
			} catch (Throwable e) {
				log.warn(this.getServiceName() + " 服务执行异常 ", e);
			}
		}

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			log.warn(this.getServiceName() + " Exception, ", e);
		}

		this.doCommit();

		log.info(this.getServiceName() + " 服务已关闭");
	}

	@Override
	protected void onWaitEnd() {
		// 当前工作线程阻塞释放后做的事情
	}

	protected abstract void doCommit();

	@Override
	public String getServiceName() {
		return MonitorLongPollingRunnable.class.getSimpleName() + "-自定义线程-" + serviceName;
	}

	@Override
	public long getJointime() {
		return 1000 * 60 * 5;
	}

}
