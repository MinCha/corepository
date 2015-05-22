package com.github.corepo.client;

public class WritebackEventNotifier implements KeyFunction {

    private LRUKeyUpdateTime keyUpdateTime;

    public WritebackEventNotifier(LRUKeyUpdateTime keyUpdateTime) {
        this.keyUpdateTime = keyUpdateTime;
    }

    public void execute(String key) {
        keyUpdateTime.notifyWritebacked(key, System.currentTimeMillis());
    }
}
