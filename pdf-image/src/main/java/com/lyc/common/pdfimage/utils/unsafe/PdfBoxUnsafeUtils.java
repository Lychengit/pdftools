package com.lyc.common.pdfimage.utils.unsafe;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.pdmodel.DefaultResourceCache;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.Map;

/**
 * @author : 		刘勇成
 * @description :
 * @date : 		2022/11/26 19:07
 */
public class PdfBoxUnsafeUtils {

	protected static final Logger logger = LoggerFactory.getLogger(PdfBoxUnsafeUtils.class);

	public static final long COS_DOCUMENT_OBJECT_POOL_OFFSET;
	public static final long DEFAULT_RESOURCE_CACHE_XOBJECTS_OFFSET;
	public static final long PDFRENDERER_PAGEIMAGE_OFFSET;

	static {
		try {
			COS_DOCUMENT_OBJECT_POOL_OFFSET = UnsafeAccess.fieldOffset(COSDocument.class, "objectPool");
			DEFAULT_RESOURCE_CACHE_XOBJECTS_OFFSET = UnsafeAccess.fieldOffset(DefaultResourceCache.class, "xobjects");
			PDFRENDERER_PAGEIMAGE_OFFSET = UnsafeAccess.fieldOffset(PDFRenderer.class, "pageImage");
		} catch (Throwable ex) {
			logger.error("当前类初始化异常信息：", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static Map<COSObjectKey, COSObject> getCOS_DOCUMENT_OBJECT_POOL(COSDocument document){
		Map<COSObjectKey, COSObject> map = null;
		Object object = UnsafeAccess.UNSAFE.getObject(document, COS_DOCUMENT_OBJECT_POOL_OFFSET);
		if (object != null){
			map = (Map<COSObjectKey, COSObject>) object;
		}
		return map;
	}

	public static Map<COSObject, SoftReference<PDXObject>> getDEFAULT_RESOURCE_CACHE_XOBJECTS(DefaultResourceCache resourceCache){
		Map<COSObject, SoftReference<PDXObject>> map = null;
		Object object = UnsafeAccess.UNSAFE.getObject(resourceCache, DEFAULT_RESOURCE_CACHE_XOBJECTS_OFFSET);
		if (object != null){
			map = (Map<COSObject, SoftReference<PDXObject>>) object;
		}
		return map;
	}

	public static void setPdfrendererPageUnsafe(PDFRenderer pdfRenderer, BufferedImage pageImage){
		UnsafeAccess.UNSAFE.putObjectVolatile(pdfRenderer, PDFRENDERER_PAGEIMAGE_OFFSET, pageImage);
	}
}
