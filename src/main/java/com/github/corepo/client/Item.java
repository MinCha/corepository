package com.github.corepo.client;


public class Item extends BaseObject {
	static final String META_PREFIX = "_META_";  
	static final long NO_UPDATED = 0L;
	static final long NO_WRITEBACKED = 0L;
	private String key;
	private String value;
	private boolean integer = false;
	private long lastUpdatedTimeInMillis;
	@SuppressWarnings("unused")
	private long lastWritebackedTimeInMillis;
	
	public Item(String key, String value) {
		this(key, value, NO_UPDATED, NO_WRITEBACKED);
	}

	public Item(String key, int value) {
		this(key, String.valueOf(value), NO_UPDATED, NO_WRITEBACKED);
		this.integer = true;
	}

	public Item(String key, int value, long lastUpdatedTime) {
		this(key, String.valueOf(value), lastUpdatedTime, NO_WRITEBACKED);
		this.integer = true;
	}

	public Item(String key, String value, long lastUpdatedTime) {
		this(key, value, lastUpdatedTime, NO_WRITEBACKED);
	}

	public Item(String key, int value, long lastUpdatedTime,
			long lastWritebackedTime) {
		this(key, String.valueOf(value), lastUpdatedTime, NO_WRITEBACKED);
		this.integer = true;
	}

	public Item(String key, String value, long lastUpdatedTime,
			long lastWritebackedTime) {
		this.key = key;
		this.value = value;
		this.lastUpdatedTimeInMillis = lastUpdatedTime;
		this.lastWritebackedTimeInMillis = lastWritebackedTime;
	}

	public String getKey() {
		return key;
	}

	public String getValueAsString() {
		return value;
	}

	public int getValueAsInt() {
		return Integer.parseInt(value);
	}

	public boolean isInteger() {
		return integer;
	}

	public boolean isNotFound() {
		return value == null;
	}

	public static Item withNoValue(String key) {
		return new Item(key, null);
	}

	public boolean isUpdatedAfterPulling() {
		return this.lastUpdatedTimeInMillis != NO_UPDATED;
	}

	public long getLastUpdatedTime() {
		return this.lastUpdatedTimeInMillis;
	}
}