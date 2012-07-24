package com.github.corepo.client;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Unlocker {
	private static final Logger LOG = LoggerFactory.getLogger(Unlocker.class);
	private ArrayList<UnlockRequest> requests = new ArrayList<UnlockRequest>();
	private CoRepository coRepository;
	private boolean activation = true;

	public Unlocker(CoRepository coRepository) {
		this.coRepository = coRepository;
	}

	public void active() {
		Thread t= new Thread(new DeplayedUnlocker());
		t.setDaemon(true);
		t.start();
		Runtime.getRuntime().addShutdownHook(new UnlockerShutdownHook());
	}

	public void requestUnlock(String key) {
		this.requestUnlock(key, 2000);
	}

	public void requestUnlock(String key, int timeInMillis) {
		requests.add(new UnlockRequest(key, timeInMillis));
	}

	private class DeplayedUnlocker implements Runnable {
		public void run() {
			try {
				while (activation) {
					unlockAllItemsOvered();
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
			}
		}

		private void unlockAllItemsOvered() {
			@SuppressWarnings("unchecked")
			List<UnlockRequest> copy = (List<UnlockRequest>) requests
					.clone();
			for (UnlockRequest each : copy) {
				if (each.isUnlockable() == false) {
					continue;
				}

				if (coRepository.exists(each.key)) {
					unlockWithRetry(each, 3);
				}
				requests.remove(each);
			}
		}
	}

	private void unlockWithRetry(UnlockRequest each, int retryCount) {
		for (int i = 0; i < retryCount; i++) {
			boolean result = coRepository.unlock(each.key);
			if (result) {
				return;
			}
		}
	}

	private class UnlockerShutdownHook extends Thread {
		public void run() {
			LOG.info("ShutdownHook Starts, wating count is " + requests.size());
			for (UnlockRequest each : requests) {
				unlockWithRetry(each, 3);
			}
			LOG.info("ShutdownHook ends");
		}
	}

	private class UnlockRequest {
		private String key;
		private long requestedTime;
		private long timeInMillis;

		private UnlockRequest(String key, long timeInMillis) {
			this.key = key;
			this.requestedTime = System.currentTimeMillis();
			this.timeInMillis = timeInMillis;
		}

		private boolean isUnlockable() {
			return requestedTime + timeInMillis < System.currentTimeMillis();
		}
	}
}
