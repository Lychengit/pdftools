package com.lyc.common.pdfimage.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : 		刘勇成
 * @description :
 * @date : 		2023/3/29 16:01
 */
public class SingletonExecutorFactory {
	private static final Logger log = LoggerFactory.getLogger(SingletonExecutorFactory.class);

	private final String msg;

	private final TPConfig TP_CONFIG = new TPConfig();
	// 注意：只能添加线程池，不能减少线程池
	private volatile List<SingletonExecutor> selects;

	public SingletonExecutorFactory(String msg) {
		this.msg = msg;
		selects = buildSelector();
	}

	private List<SingletonExecutor> buildSelector(){
		List<SingletonExecutor> selects = new ArrayList<>();
		for (int i = 0; i < TP_CONFIG.gettPNum(); i++) {
			selects.add(new SingletonExecutor("【"+ this.msg+"】" + i, TP_CONFIG.gettPCapacity()));
		}
		log.info("构建【{}】的单线程线程池集合，线程池的个数【{}】，队列的长度【{}】", msg, selects.size(), TP_CONFIG.gettPCapacity());
		return selects;
	}

	public void rebuildThreadPool() {
		selects = buildSelector();
	}

	public String getMsg() {
		return msg;
	}

	public TPConfig getTpConfig() {
		return TP_CONFIG;
	}

	public List<SingletonExecutor> getSelects() {
		return selects;
	}

}
