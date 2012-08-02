package com.github.corepo.client;

public class UnlockerShutdownHook implements Runnable {
	private Unlocker unlocker;

	public UnlockerShutdownHook(Unlocker unlocker) {
		this.unlocker = unlocker;
	}

	public void run() {
		unlocker.unlockAll();
	}
}
