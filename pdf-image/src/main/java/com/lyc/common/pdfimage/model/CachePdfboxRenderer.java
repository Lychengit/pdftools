package com.lyc.common.pdfimage.model;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @description : Pdfbox的转换实例体
 *
 * @author : 		刘勇成
 * @date : 		2023/3/3 15:05
 *
 */
public class CachePdfboxRenderer extends PDFRenderer {

	private OptRequest optRequest = new OptRequest();

	public boolean cancel(){
		return optRequest.cancel();
	}

	public boolean isCancelled() {
		return optRequest.isCancelled();
	}

	private final AtomicLong renderCout = new AtomicLong(0);

	private final String previewid;

	// 绑定的转换队列的序号
	private final int queueNo;

	private final ReentrantReadWriteLock readWriteLock;

	public CachePdfboxRenderer(PDDocument document, String previewid, int queueNo) {
		super(document);
		this.previewid = previewid;
		this.queueNo = queueNo;
		this.readWriteLock = new ReentrantReadWriteLock(false);
	}

	public PDDocument getPDDocument(){
		return this.document;
	}

	public String getPreviewid() {
		return previewid;
	}

	public int getQueueNo() {
		return queueNo;
	}

	/**
	 * 构建自己的画图工具（如处理pdf里面的图片子采样的粒度）
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	@Override
	protected PageDrawer createPageDrawer(PageDrawerParameters parameters) throws IOException {
		PDDocument pdDocument = getPDDocument();
		if (pdDocument == null){
			throw new RuntimeException("创建页绘画器时，PDDocument为空！");
		}
		COSDocument document = pdDocument.getDocument();
		if (document == null){
			throw new RuntimeException("创建页绘画器时，COSDocument为空！");
		}
		PdfboxPageDrawer pageDrawer = new PdfboxPageDrawer(parameters, this.previewid);
		pageDrawer.setAnnotationFilter(getAnnotationsFilter());
		return pageDrawer;
	}

	@Override
	public BufferedImage renderImage(int pageIndex, float scale) throws IOException {
		long incrementAndGet = renderCout.incrementAndGet();
		if (incrementAndGet >= 100000000) {
			renderCout.getAndSet(0);
		}
		return super.renderImage(pageIndex, scale);
	}

	public long getRenderCoutNum() {
		return renderCout.get();
	}

	public ReentrantReadWriteLock getReadWriteLock() {
		return readWriteLock;
	}

	// 当前转换组是否还存在任务
	public boolean hasTask(){
		ReentrantReadWriteLock readWriteLock = this.getReadWriteLock();
		return readWriteLock.hasQueuedThreads()||
				readWriteLock.getReadLockCount() > 0;
	}
}