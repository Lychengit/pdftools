package com.lyc.common.pdfimage.model;

import com.lyc.common.pdfimage.utils.weakmap.MyConcurrentReferenceHashMap;

import java.util.Map;

public class WeakHashLockEntry<T> {
    private static final int DEFAULT_INITIAL_CAPACITY = 64;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_CONCURRENCY_LEVEL = 64;
    private static final MyConcurrentReferenceHashMap.ReferenceType DEFAULT_REFERENCE_TYPE =
            MyConcurrentReferenceHashMap.ReferenceType.SOFT;

    private final MyConcurrentReferenceHashMap<T, ReentrantLockEntry> referenceHashMap;

    public WeakHashLockEntry() {
        this.referenceHashMap = new MyConcurrentReferenceHashMap<>(DEFAULT_INITIAL_CAPACITY,
                DEFAULT_LOAD_FACTOR,
                DEFAULT_CONCURRENCY_LEVEL,
                DEFAULT_REFERENCE_TYPE);
    }

    public WeakHashLockEntry(int concurrencyLevel,
							 MyConcurrentReferenceHashMap.ReferenceType referenceType) {
        this.referenceHashMap = new MyConcurrentReferenceHashMap<>(DEFAULT_INITIAL_CAPACITY,
                DEFAULT_LOAD_FACTOR,
                concurrencyLevel,
                referenceType);
    }

    public MyConcurrentReferenceHashMap<T, ReentrantLockEntry> getReferenceHashMap() {
        return referenceHashMap;
    }

    public ReentrantLockEntry computeIfAbsent(T key) {
        return this.referenceHashMap.computeIfAbsent(key, lock -> new ReentrantLockEntry(true));
    }

    public ReentrantLockEntry get(T key) {
        return this.referenceHashMap.get(key);
    }

    public ReentrantLockEntry remove(T key) {
        return this.referenceHashMap.remove(key);
    }

    public Map.Entry<T, ReentrantLockEntry> getEntry(Object key) {
        return this.referenceHashMap.getEntry(key);
    }


}
