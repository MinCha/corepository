package com.github.writeback.client;


public class Item extends BaseObject {
	private String key;
	private String value;
	private boolean integer = false;
	
	public Item(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public Item(String key, int value) {
		this.key = key;
		this.value = String.valueOf(value);
		this.integer = true;
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
}