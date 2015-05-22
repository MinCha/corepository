package com.github.corepo.client;

public class UpdateTime {
    private long timeInMillis;

    public UpdateTime(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public void update(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public long time() {
        return timeInMillis;
    }
}
