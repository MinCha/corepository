package com.github.corepo.client.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.github.corepo.client.CoRepositoryClient;
import com.github.corepo.client.Item;
import com.github.corepo.client.LocalMemoryCoRepository;
import com.github.corepo.client.NonExistentKeyException;
import com.github.corepo.client.OriginalRepository;
import com.github.corepo.client.TimeoutException;

public class CoRepositoryClientIntegrationTest {
	private CoRepositoryClient sut = new CoRepositoryClient(
			new LocalMemoryCoRepository(), new OriginalRepository() {
				public void writeback(Item item) {
				}

				public Item read(String passedKey) {
					return Item.withNoValue(passedKey);
				}
			});
	private int nonExistenceKey = 0;
	private int timeout = 0;

	@Test
	public void watingThreadsShouldReciveTimeoutException_WhenThereIsNoKeyOnOriginalRepository() {
		final String key = "non-existing";
		final int clientCount = 100;
		ExecutorService executors = Executors.newFixedThreadPool(1000);
		List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
		for (int i = 0; i < clientCount; i++) {
			tasks.add(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					try {
						sut.increase(key);
					} catch (NonExistentKeyException e) {
						nonExistenceKey++;
					} catch (TimeoutException e) {
						timeout++;
					}
					return Boolean.TRUE;
				}
			});
		}
		try {
			executors.invokeAll(tasks);
		} catch (InterruptedException e) {
		}

		assertThat(nonExistenceKey, is(1));
		assertThat(timeout, is(clientCount - 1));
	}
}
