package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.model.monitor.OptRequest;
import com.lyc.common.pdfimage.utils.FileUtils;
import com.lyc.common.pdfimage.utils.weakmap.ReferenceMapEntry;
import io.netty.util.HashedWheelTimer;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @description : Pdfbox转换实例组
 *
 * @author : 		刘勇成
 * @date : 		2023/3/3 15:05
 *
 */
public class CachePdfboxRendererGroup implements ReferenceMapEntry {


	private OptRequest optRequest = new OptRequest();

	public boolean cancel(){
		return optRequest.cancel();
	}

	public boolean isCancelled() {
		return optRequest.isCancelled();
	}

	// 源文件
	private final File file;
	// 主预览id
	private final String previewidRoot;
	// 子转换实例集合
	private final Map<String, CachePdfboxRenderer> groupMap;
	private final HashedWheelTimer hashedWheelTimer;
	// 当前读写锁用于标识某个文件（转换实例）是否有转换操作，以此来让异步监控任务判断是否要删除当前的本地源文件
	private final ReentrantReadWriteLock readWriteLock;
	private PdfboxRendererExpireRequest request;

	// 局部强引用，避免转换组还没有过期就因fullgc导致转换组entry被回收
	private Object groupEntry;

	// 局部强引用，避免转换组还没有过期就因fullgc导致转换组entry被回收
	private Map.Entry entryObj;

	// 当前pdf的源文件的大小
	private Long pdfSize;
	// 当前pdf的总页数
	private Long pdfNum;
	// 当前pdf预知的object数量
	private Long foreknewObjectSize;

	public CachePdfboxRendererGroup(File file, String previewidRoot, HashedWheelTimer hashedWheelTimer, ReentrantReadWriteLock readWriteLock) {
		this.file = file;
		this.previewidRoot = previewidRoot;
		this.groupMap = new ConcurrentHashMap<>();;
		this.hashedWheelTimer = hashedWheelTimer;
		if (readWriteLock != null){
			this.readWriteLock = readWriteLock;
		} else {
			this.readWriteLock = new ReentrantReadWriteLock(false);
		}
	}

	public File getFile() {
		return file;
	}

	public String getPreviewidRoot() {
		return previewidRoot;
	}

	public HashedWheelTimer getHashedWheelTimer() {
		return hashedWheelTimer;
	}

	public ReentrantReadWriteLock getReadWriteLock() {
		return readWriteLock;
	}

	public Map<String, CachePdfboxRenderer> getGroupMap() {
		return groupMap;
	}

	public void setRequest(PdfboxRendererExpireRequest request) {
		this.request = request;
	}

	public PdfboxRendererExpireRequest getRequest() {
		return request;
	}

	public Object getGroupEntry() {
		return groupEntry;
	}

	public void setGroupEntry(Object groupEntry) {
		this.groupEntry = groupEntry;
	}

	public void continueExpireTime(){
		PdfboxRendererExpireRequest request = getRequest();
		if (request != null){
			request.continueExpireTime();
		}
	}

	public boolean isExpireTime(){
		PdfboxRendererExpireRequest request = getRequest();
		if (request != null){
			return request.isExpireTime();
		}
		return true;
	}

	public boolean isMapExpireTime(){
		PdfboxRendererExpireRequest request = getRequest();
		if (request != null){
			return request.isGroupMapExpireTime();
		}
		return true;
	}

	// 当前转换组是否还存在任务
	public boolean hasTask(){
		ReentrantReadWriteLock readWriteLock = this.getReadWriteLock();
		return readWriteLock.hasQueuedThreads()||
				readWriteLock.getReadLockCount() > 0;
	}

	public boolean isValidFile(){
		return FileUtils.isValidFile(file);
	}

	public Long getPdfSize() {
		return pdfSize;
	}

	public void setPdfSize(Long pdfSize) {
		this.pdfSize = pdfSize;
	}

	public Long getPdfNum() {
		return pdfNum;
	}

	public void setPdfNum(Long pdfNum) {
		this.pdfNum = pdfNum;
	}

	public Long getForeknewObjectSize() {
		return foreknewObjectSize;
	}

	public void setForeknewObjectSize(Long foreknewObjectSize) {
		this.foreknewObjectSize = foreknewObjectSize;
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