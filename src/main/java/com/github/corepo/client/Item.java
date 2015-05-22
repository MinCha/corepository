package com.github.corepo.client;

import java.io.Serializable;

public class Item extends BaseObject implements Serializable {
    private static final long serialVersionUID = 8494719821834512432L;
    private String key;
    private Object value;
    private boolean integer = false;

    public Item(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Item(String key, Integer value) {
        this.key = key;
        this.value = value;
        this.integer = true;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public String getValueAsString() {
        return (String) value;
    }

    public int getValueAsInt() {
        return (Integer) value;
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