package com.github.corepo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Unlocker {
	private static final Logger LOG = LoggerFactory.getLogger(Unlocker.class);
	private CoRepository coRepository;

	public Unlocker(CoRepository coRepository) {
		this.coRepository = coRepository; 
	}

	public void unlockAfter(String key, int timeInMillis) {
		if (coRepository.exists(key) == false) {
			return;
		}
		new Thread(new DeplayedUnlocker(key, timeInMillis)).start();
	}
	
	private class DeplayedUnlocker implements Runnable {
		private String key;
		private int timeInMillits;
		
		private DeplayedUnlocker(String key, int timeInMillis) {
			this.key = key;
			this.timeInMillits = timeInMillis;
		}
		
		public void run() {
			try {
				Thread.sleep(timeInMillits);
			} catch (InterruptedException ignored) {
			}
			
			int retryCoun = 3;
			for (int i = 0; i < retryCoun; i++) {
				if (coRepository.unlock(key)) {
					return;
				}
			}
			
			LOG.error("Failed unlock '{}'", key);
		}
	}

	public void unlock(String key) {
		this.unlockAfter(key, 2000);
	}
}
