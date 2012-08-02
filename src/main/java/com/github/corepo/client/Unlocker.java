package com.github.corepo.client;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Unlocker implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(Unlocker.class);
	private LinkedList<UnlockRequest> requests = new LinkedList<UnlockRequest>();
	private CoRepository coRepository;
	private boolean running = true;
	private final Object lock = new Object();

	public Unlocker(CoRepository coRepository) {
		this.coRepository = coRepository;
		runBackgroundUnlocker();
		Runtime.getRuntime().addShutdownHook(
				new Thread(new UnlockerShutdownHook(this)));
	}

	public void stop() {
		running = false;
		synchronized (lock) {
			lock.notify();
			unlockAll();
		}
	}

	public void requestUnlock(String key) {
		this.requestUnlock(key, 2000);
	}

	public void requestUnlock(String key, int timeInMillis) {
		if (running == false) {
			throw new IllegalStateException(
					"Unlocker is not active. Please run active method before call.");
		}
		synchronized (lock) {
			requests.add(new UnlockRequest(key, timeInMillis));
		}
	}

	public void run() {
		try {
			while (running) {
				synchronized (lock) {
					@SuppressWarnings("unchecked")
					List<UnlockRequest> copy = (List<UnlockRequest>) requests
							.clone();
					for (UnlockRequest each : copy) {
						if (each.isUnlockable() == false) {
							continue;
						} else {
							if (coRepository.exists(each.key())) {
								unlockWithRetry(each, 3);
							}
							requests.remove(each);
						}
					}

					lock.wait(100);
				}
			}
		} catch (InterruptedException e) {
		}
	}

	@SuppressWarnings("unchecked")
	void unlockAll() {
		if (coRepository.isConnected() == false) {
			return;
		}

		synchronized (lock) {
			LOG.info("UnlockerHook has just started, wating count is "
					+ requests.size());
			List<UnlockRequest> targets = (List<UnlockRequest>) requests
					.clone();
			for (UnlockRequest each : targets) {
				unlockWithRetry(each, 3);
			}
			LOG.info("UnlockerHook has just ended");
		}
	}

	private void unlockWithRetry(UnlockRequest each, int retryCount) {
		for (int i = 0; i < retryCount; i++) {
			boolean result = coRepository.unlock(each.key());
			if (result) {
				return;
			}
		}
	}

	private void runBackgroundUnlocker() {
		Thread t = new Thread(this);
		t.setName("CoRepository Unlocker by Thread"
				+ Thread.currentThread().getName());
		t.start();
	}
}
