package com.lyc.common.pdfimage.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author : 		刘勇成
 * @description : 线程池工厂
 * @date : 		2023/3/2 14:29
 */
public class ThreadPoolExecutorFactory {

	private static final Logger log = LoggerFactory.getLogger(ThreadPoolExecutorFactory.class);
	private final String msg;
	// 线程池配置
	private TPConfig TP_CONFIG = new TPConfig();
	private volatile ThreadPoolExecutor threadPoolExecutor;

	public ThreadPoolExecutorFactory(String msg) {
		this.msg = msg;
		threadPoolExecutor = buildThreadPool(TP_CONFIG);
	}

	public ThreadPoolExecutorFactory(String msg, TPConfig tpConfig) {
		this.msg = msg;
		this.TP_CONFIG = tpConfig;
		threadPoolExecutor = buildThreadPool(tpConfig);
	}

	private ThreadPoolExecutor buildThreadPool(TPConfig tpConfig) {
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
				.setNameFormat("-【"+ this.msg+"】-threadpool-%d")
				.setUncaughtExceptionHandler((t, e) -> {
					log.error("【"+ this.msg+"】出现异常", e);
				})
				.build();

		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(tpConfig.gettPCorePoolSize(), tpConfig.gettPCorePoolSize(), tpConfig.gettPKeepAliveTime(), tpConfig.gettPUnit(),
				new LinkedBlockingDeque<>(tpConfig.gettPCapacity()),
				threadFactory,
				new ThreadPoolExecutor.AbortPolicy()
		);

		log.info("构建【{}】的线程池，线程的个数【{}】，队列的长度【{}】", msg, tpConfig.gettPCorePoolSize(), tpConfig.gettPCapacity());
		return threadPoolExecutor;
	}

	public String getMsg() {
		return msg;
	}

	public TPConfig getTP_CONFIG() {
		return TP_CONFIG;
	}

	public void rebuildThreadPool() {
		threadPoolExecutor = buildThreadPool(TP_CONFIG);
	}

	public ThreadPoolExecutor getThreadPool() {
		return threadPoolExecutor;
	}


}
