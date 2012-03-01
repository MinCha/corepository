package com.github.writeback.client;

import com.github.writeback.client.support.BaseObject;

public class WriteBackItem extends BaseObject {
	private Object key;
	private Object value;
	
	public WriteBackItem(Object key, Object value) {
		this.key = key;
		this.value = value;
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public long getValueAsLong() {
		return (Long) value;
	}
}
