package com.github.corepo.client;

@SuppressWarnings("serial")
public class NonExistentKeyException extends RuntimeException {
    public NonExistentKeyException(ItemKey key) {
        super("Key does not exist : " + key.getKey());
    }
}
