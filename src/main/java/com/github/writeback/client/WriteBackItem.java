package com.github.writeback.client;

import com.github.writeback.client.exception.IllegalKeyException;
import com.github.writeback.client.support.BaseObject;

public class WriteBackItem extends BaseObject {
	final static int MAXIMUM_KEY_LENGTH = 30;
	
	private String key;
	private Object value;
	
	public WriteBackItem(String key, Object value) {
		if (key.length() > MAXIMUM_KEY_LENGTH) {
			throw new IllegalKeyException("over length : " + key);
		}
		
		this.key = key;
		this.value = value;
	}

	public static int getMaximumKeyLength() {
		return MAXIMUM_KEY_LENGTH;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public long getValueAsLong() {
		return (Long) value;
	}
}
