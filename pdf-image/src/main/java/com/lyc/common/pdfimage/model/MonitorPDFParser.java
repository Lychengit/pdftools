package com.lyc.common.pdfimage.model;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessStreamCache;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author : 		刘勇成
 * @description : 重写pdfbox解析类，为了快速获取pdf的某些数据，如总页数
 * @date : 		2022/11/16 16:24
 */
public class MonitorPDFParser extends PDFParser {

	protected static final Logger logger = LoggerFactory.getLogger(MonitorPDFParser.class);

	private final AtomicLong COSStreamCout = new AtomicLong(0);

	public MonitorPDFParser(RandomAccessRead source) throws IOException {
		super(source);
	}

	public MonitorPDFParser(RandomAccessRead source, String decryptionPassword) throws IOException {
		super(source, decryptionPassword);
	}

	public MonitorPDFParser(RandomAccessRead source, String decryptionPassword, InputStream keyStore, String alias) throws IOException {
		super(source, decryptionPassword, keyStore, alias);
	}

	public MonitorPDFParser(RandomAccessRead source, String decryptionPassword, InputStream keyStore, String alias, RandomAccessStreamCache.StreamCacheCreateFunction streamCacheCreateFunction) throws IOException {
		super(source, decryptionPassword, keyStore, alias, streamCacheCreateFunction);
	}

	/**
	 *
	 * @param trailer
	 * @return true 如果文档加密.
	 */
	public boolean isEncrypted(COSDictionary trailer)
	{
		return trailer != null && trailer.getCOSDictionary(COSName.ENCRYPT) != null;
	}


	public AtomicLong getCOSStreamCout() {
		return COSStreamCout;
	}

	public long getCOSStreamCoutNum() {
		return COSStreamCout.get();
	}

	public COSDictionary parseTrailer() throws IOException {
		try {
			return retrieveTrailer();
		} finally {
			source.seek(0);
		}
	}

	// 没有值会返回-1
	public long parseObjectSizeByTrailer(COSDictionary trailer) throws IOException {
		return trailer.getLong(COSName.SIZE);
	}

	// 获取root节点
	public COSDictionary parseRoot(COSDictionary trailer) throws IOException {
		try {
			COSDictionary root = trailer.getCOSDictionary(COSName.ROOT);
			if (root == null)
			{
				throw new IOException("Missing root object specification in trailer.");
			}
			// in some pdfs the type value "Catalog" is missing in the root object
			if (isLenient() && !root.containsKey(COSName.TYPE))
			{
				root.setItem(COSName.TYPE, COSName.CATALOG);
			}
			// check pages dictionaries
			checkPages(root);
			return root;
		} finally {
			source.seek(0);
		}
	}

//	// 重写解析COSStream的方法，统计加载pdfbox转换实例过程中COSStream的个数（其实是想统计objectpool里面的COSStream数）
//	protected COSStream parseCOSStream(COSDictionary dic) throws IOException
//	{
//		COSStream stream = super.parseCOSStream(dic);
//		COSStreamCout.incrementAndGet();
//		return stream;
//	}

}
