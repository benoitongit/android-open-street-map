package com.android.lib.map.osm;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;
	private int maxCapacity;

	public LRUMap(int initialCapacity, int maxCapacity) {
		super(initialCapacity, 0.75f, true);
		this.maxCapacity = maxCapacity;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > this.maxCapacity;
	}
}
