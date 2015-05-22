package com.github.corepo.client;

public class ItemKey extends BaseObject {
    public static final String DEFAULT_NAMESPACE = "default";
    public static final String KEY_DELIM = ":";
    public static final String LOCK_PREFIX = "_CO_REPOSITORY_LOCK_FOR_";
    private String nameSpace;
    private String id;

    public ItemKey(String nameSpace, String id) {
        this.nameSpace = nameSpace;
        this.id = id;
    }

    public ItemKey(String id) {
        this(DEFAULT_NAMESPACE, id);
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return nameSpace + KEY_DELIM + id;
    }

    public ItemKey convertToLockedKey() {
        return new ItemKey(LOCK_PREFIX + nameSpace, id);
    }
}
