package com.lyc.common.pdfimage.service.impl;

import com.lyc.common.pdfimage.PdfBoxConvertorProvider;
import com.lyc.common.pdfimage.model.CachePdfboxRenderer;
import com.lyc.common.pdfimage.model.CachePdfboxRendererGroup;
import com.lyc.common.pdfimage.model.PbConvertParamConfig;
import com.lyc.common.pdfimage.model.SingletonExecutor;
import com.lyc.common.pdfimage.service.PdfboxAbstractLoadBalance;
import com.lyc.common.pdfimage.utils.LBalanceUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : 		刘勇成
 * @description : 加权最小转换任务数算法
 * @date : 		2023/4/4 11:24
 */
public class PbWeightedLeastTaskRule implements PdfboxAbstractLoadBalance {

	private AtomicInteger nextServerCyclicCounter = new AtomicInteger(0);
	private AtomicInteger nextServerCyclicCounter1 = new AtomicInteger(0);

	@Override
	public String selectPreviewid(CachePdfboxRendererGroup group, List<SingletonExecutor> selects, PbConvertParamConfig convertParamConfig) {
		if (selects.size() <= 0) {
			throw new RuntimeException("不存在任何转换队列");
		}
		String previewidRoot = group.getPreviewidRoot();
		long pdDocumentMaxSize = convertParamConfig.getPdDocumentMaxSize();
		if (pdDocumentMaxSize == 1){
			return previewidRoot;
		}

		//主转换实例为空，则先加载主转换实例，将任务交给主转换实例
		Integer maxTaskSize = null;
		Integer minTaskSize = null;
		String minPreviewidChild = previewidRoot;
		String previewidChild = null;
		for (int i = 0; i < pdDocumentMaxSize; i++) {
			if (i == 0) {
				previewidChild = previewidRoot;
			} else {
				previewidChild = previewidRoot+ "_"+ i;
			}
			Map<String, CachePdfboxRenderer> childMap = PdfBoxConvertorProvider.getChildMap(group);
			if (childMap != null) {
				CachePdfboxRenderer renderer = childMap.get(previewidChild);
				if (PdfBoxConvertorProvider.isValid(renderer)) {
					int queueNo = renderer.getQueueNo();
					SingletonExecutor executor = selects.get(queueNo);
					if (executor != null) {
						int taskSize = executor.taskSize();
						// 否则，做大小的比对，保留任务数较小的队列序号对应的子预览id
						if (minTaskSize == null) {
							minTaskSize = taskSize;
						}
						if (maxTaskSize == null) {
							maxTaskSize = taskSize;
						}
						if (taskSize < minTaskSize) {
							minTaskSize = taskSize;
							minPreviewidChild = previewidChild;
						} else if (taskSize > maxTaskSize) {
							maxTaskSize = taskSize;
						}
					}
				}
			}
		}
		// 如果转换队列不平衡，则返回最少任务的队列序号，否则基于轮询算法处理
		if (maxTaskSize != null && minTaskSize != null) {
			boolean isLBalance = true;
			int gapSize = maxTaskSize - minTaskSize;
			if (maxTaskSize > convertParamConfig.getSingletonLBMaxfloor() && gapSize > 0 && maxTaskSize <= (gapSize * 3)) {
				isLBalance = false;
			}
			if (!isLBalance) {
				return minPreviewidChild;
			}
		}

		// 轮询策略
		int index = LBalanceUtils.incrementAndGetModulo(nextServerCyclicCounter, (int) pdDocumentMaxSize);
		if (index == 0){
			return previewidRoot;
		} else {
			return previewidRoot+ "_"+ index;
		}
	}

	@Override
	public int selectQueueNo(List<SingletonExecutor> selects, PbConvertParamConfig convertParamConfig) {

		if (selects.size() <= 0) {
			throw new RuntimeException("不存在任何转换队列");
		}

		if (selects.size() == 1) {
			return 0;
		}
		int firstNo = 0;
		SingletonExecutor executor0 = selects.get(firstNo);
		if (executor0 == null) {
			throw new RuntimeException("当前转换执行器为空");
		}
		int taskSize0 = executor0.taskSize();
		int maxTaskSize = taskSize0;
		int minTaskSize = taskSize0;
		int minExecutorNo = firstNo;
		for (int i = 1; i < selects.size(); i++) {
			SingletonExecutor executor = selects.get(i);
			if (executor != null) {
				int taskSize = executor.taskSize();
				// 否则，做大小的比对，保留任务数较小的队列序号
				if (taskSize < minTaskSize) {
					minTaskSize = taskSize;
					minExecutorNo = i;
				} else if (taskSize > maxTaskSize){
					maxTaskSize = taskSize;
				}
			}
		}
		// 如果转换队列不平衡，则返回最少任务的队列序号，否则基于轮询算法处理
		boolean isLBalance = true;
		int gapSize = maxTaskSize - minTaskSize;
		if (minTaskSize > convertParamConfig.getSingletonLBMinfloor() && gapSize > 0 && gapSize >= (minTaskSize * 3)){
			isLBalance = false;
		}
		if (!isLBalance) {
			return minExecutorNo;
		}
		return LBalanceUtils.incrementAndGetModulo(nextServerCyclicCounter1, selects.size());
	}
}
