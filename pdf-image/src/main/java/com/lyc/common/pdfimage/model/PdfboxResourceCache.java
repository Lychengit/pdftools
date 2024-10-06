package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.utils.unsafe.PdfBoxUnsafeUtils;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.DefaultResourceCache;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;

import java.lang.ref.SoftReference;
import java.util.Map;

/**
 * @author : 		刘勇成
 *
 * @description :
 *
 *
 *     pdfbox使用DefaultResourceCache用于缓存一些可复用的数据，这里发现压测指定文档时出现复用的图片实例造成了内存溢出。
 *
 *
 * @date : 		2023/6/15 15:18
 */
public class PdfboxResourceCache extends DefaultResourceCache {

//	@Override
//	public void put(COSObject indirect, PDXObject xobject) {
//
//	}
//
//	@Override
//	public PDXObject getXObject(COSObject indirect) {
//		return null;
//	}

	public Map<COSObject, SoftReference<PDXObject>> getXObjectMaps(){
		return PdfBoxUnsafeUtils.getDEFAULT_RESOURCE_CACHE_XOBJECTS(this);
	}

	public void clearXObject() {
		Map<COSObject, SoftReference<PDXObject>> resourceCacheXobjects = getXObjectMaps();
		resourceCacheXobjects.clear();
	}

	public int countXObjectSize() {
		Map<COSObject, SoftReference<PDXObject>> resourceCacheXobjects = getXObjectMaps();
		return resourceCacheXobjects.size();
	}
}
