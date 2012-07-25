package com.github.corepo.client;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Unlocker {
	private static final Logger LOG = LoggerFactory.getLogger(Unlocker.class);
	private ArrayList<UnlockRequest> requests = new ArrayList<UnlockRequest>();
	private CoRepository coRepository;
	private boolean activation = false;
	private final Object lock = new Object();

	public Unlocker(CoRepository coRepository) {
		this.coRepository = coRepository;
	}

	public void active() {
		activation = true;
		Thread t = new Thread(new DeplayedUnlocker());
		t.setDaemon(true);
		t.start();
		Runtime.getRuntime().addShutdownHook(new UnlockerShutdownHook());
	}

	public void requestUnlock(String key) {
		this.requestUnlock(key, 2000);
	}

	public void requestUnlock(String key, int timeInMillis) {
		if (activation == false) {
			throw new IllegalStateException(
					"Unlocker is not active. Please run active method before call.");
		}

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
			synchronized (lock) {
				@SuppressWarnings("unchecked")
				List<UnlockRequest> copy = (List<UnlockRequest>) requests
						.clone();
				for (UnlockRequest each : copy) {
					if (each.isUnlockable() == false) {
						continue;
					} else {
						if (coRepository.exists(each.key)) {
							unlockWithRetry(each, 3);
						}
						requests.remove(each);
					}
				}
			}
		}
	}

	private void unlockWithRetry(UnlockRequest each, int retryCount) {
		for (int i = 0; i < retryCount; i++) {
			//TODO Why null? Please, review this condition.
			if (each == null) {
				return;
			}
			
			boolean result = coRepository.unlock(each.key);
			if (result) {
				return;
			}
		}
	}

	private class UnlockerShutdownHook extends Thread {
		@SuppressWarnings({ "unchecked" })
		public void run() {
			synchronized (lock) {
				LOG.info("ShutdownHook Starts, wating count is "
						+ requests.size());
				List<UnlockRequest> targets = (List<UnlockRequest>) requests
						.clone();
				for (UnlockRequest each : targets) {
					unlockWithRetry(each, 3);
				}
				LOG.info("ShutdownHook ends");
			}
		}
	}

	private class UnlockRequest extends BaseObject {
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
