package com.github.corepo.client;

import java.util.ArrayList;
import java.util.List;

public class Unlocker {
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

					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
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
