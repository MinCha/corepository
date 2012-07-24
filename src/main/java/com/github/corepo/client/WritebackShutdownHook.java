package com.github.corepo.client;

public class WritebackShutdownHook extends Thread {
	private LRUKeyUpdateTime keyUpdateTime;

	public WritebackShutdownHook(LRUKeyUpdateTime keyUpdateTime) {
		this.keyUpdateTime = keyUpdateTime;
	}

	@Override
	public void run() {
		keyUpdateTime.removeAll();
	}
}
