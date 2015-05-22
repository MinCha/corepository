package com.github.corepo.client;

@SuppressWarnings("serial")
public class NonExistentKeyException extends RuntimeException {
    public NonExistentKeyException(String key) {
        super("Key does not exist : " + key);
    }
}
