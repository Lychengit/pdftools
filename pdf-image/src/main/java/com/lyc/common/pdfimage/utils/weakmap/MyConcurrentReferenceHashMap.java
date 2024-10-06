package com.lyc.common.pdfimage.utils.weakmap;

import java.util.Map;

/**
 * @author : 		刘勇成
 * @description :
 *
 * 继承spring的ConcurrentReferenceHashMap
 *
 *
 *
 * @date : 		2023/6/14 17:55
 */
public class MyConcurrentReferenceHashMap<K, V> extends ConcurrentReferenceHashMap<K, V> {

	public MyConcurrentReferenceHashMap() {
	}

	public MyConcurrentReferenceHashMap(int initialCapacity, float loadFactor, int concurrencyLevel, ReferenceType referenceType) {
		super(initialCapacity, loadFactor, concurrencyLevel, referenceType);
	}

	public Reference<K, V> getReferenceByKey( Object key, Restructure restructure) {
		return super.getReference(key, restructure);
	}

	public Map.Entry<K, V> getEntry( Object key) {
		Reference<K, V> ref = getReferenceByKey(key, Restructure.NEVER);
		Entry<K, V> entry = (ref != null ? ref.get() : null);
		return entry;
	}
}
