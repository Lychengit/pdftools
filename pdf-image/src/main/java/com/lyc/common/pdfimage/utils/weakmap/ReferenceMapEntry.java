package com.lyc.common.pdfimage.utils.weakmap;

import java.util.Map;

/**
 * @author : 		刘勇成
 * @description :
 * @date : 		2024/6/17 16:56
 */
public interface ReferenceMapEntry {

	Map.Entry getEntry();

	void setEntry(Map.Entry entry);
}
