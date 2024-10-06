package com.lyc.common.pdfimage.utils;

import com.lyc.common.pdfimage.model.CachePdfboxRendererGroup;
import com.lyc.common.pdfimage.model.MonitorPDFParser;
import com.lyc.common.pdfimage.model.PdfboxResourceCache;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

/**
 * @author : 		刘勇成
 * @description : pdfbox加载pdf的自定义重写类，方便监控pdf的加载过程（这也是pdfbox加载pdf文档的入口）
 *
 * 实现逻辑：在pdfbox原来提供的PDDocument.load()静态方法的基础上加入了异步监控线程专门用于监控pdfbox加载pdf过程的逻辑。
 *
 * 监控链中的监控节点：
 *
 * 监控节点一、PdfBoxObjectPoolPageHandler用于监控pdfbox加载pdf过程中加载到的cos对象数是否超过预设值，来判断是否需要继续加载该pdf。
 *
 * 当前监控节点的目的：避免pdfbox持续加载cos对象数异常的pdf，导致内存被异常占用。
 *
 * 预设值的上限的计算公式：当前文档预设允许的COS对象数上限 = 当前文档的总页数 * 平均每页预设的允许的COS对象数上限（如平均每页最多允许存在1000个cos对象数）。
 *
 * 处理的结果：当异步监控线程监控到的文档所加载的cos对象数超过预设值，将直接关闭加载的实例，释放内存，结束主线程的加载操作。
 *
 * 具体需求流程详情可查看 https://jira.qiyuesuo.me/browse/PRIV-2377?goToView=1
 *
 *
 * @date : 		2022/11/17 14:49
 */
public class MonitorPDDocument {

	protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(MonitorPDDocument.class);

	/**
	 * 注意：本方法专用于net.qiyuesuo.common.pdf.image.PdfBoxConvertorProvider#loadBalancingToImage(java.lang.String, int, net.qiyuesuo.common.pdf.image.monitor.model.PdfRendererTask)
	 * @param file
	 * @param decryptionPassword
	 * @param keyStore
	 * @param alias
	 * @return
	 * @throws Throwable
	 */
	public static PDDocument loadPDDocumentGroup(File file, String decryptionPassword, InputStream keyStore, String alias,
												 CachePdfboxRendererGroup group) throws Throwable {

		if (file == null || !file.exists()){
			throw new RuntimeException("加载的文件不能为空或不存在~");
		}
		RandomAccessRead raFile = null;
		PDDocument pdDocument = null;
		try {
			raFile = new RandomAccessReadBufferedFile(file);
			MonitorPDFParser parser = new MonitorPDFParser(raFile, decryptionPassword, keyStore, alias,
					IOUtils.createTempFileOnlyStreamCache());
			pdDocument = parser.parse();
			pdDocument.setResourceCache(new PdfboxResourceCache());
			return pdDocument;
		} catch (Throwable e) {
			logger.error("pdfbox解析文件异常：", e);
			if (pdDocument != null){
				IOUtils.closeQuietly(pdDocument);
			}
			if (raFile != null) {
				IOUtils.closeQuietly(raFile);
			}
			throw e;
		}
	}
}
