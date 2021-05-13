package com.keyman.watcher.queue;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUQueue<K> {

    private K tail = null;
    private static final float LOAD_FACTOR = 0.75f;
    private final LinkedHashMap<K, Object> map;
    public LRUQueue(int cacheSize) {
        //根据cacheSize和加载因子计算hashmap的capactiy，+1确保当达到cacheSize上限时不会触发hashmap的扩容，
        int capacity = (int) Math.ceil(cacheSize / LOAD_FACTOR) + 1;
        map = new LinkedHashMap<K, Object>(capacity, LOAD_FACTOR, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > cacheSize;
            }
        };
    }

    public synchronized void add(K key) {
        tail = key;
        map.put(key, null);
    }

    public synchronized K get() {
        return tail;
    }

    public synchronized void remove(K key) {
        map.remove(key);
    }

    public synchronized int size() {
        return map.size();
    }

    public synchronized void clear() {
        map.clear();
    }
}
