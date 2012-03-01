package com.github.writeback.client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.writeback.client.support.FakeOriginalRepository;
import com.github.writeback.client.support.FakeVisitationDAO;
import com.github.writeback.client.support.OnlyDecreaseThread;
import com.github.writeback.client.support.OnlyIncreaseThread;

public class MultipleWriteBackClientTest {
	private List<WriteBackClient> clients = new ArrayList<WriteBackClient>();

	private final String key = "key";
	private CoRepository coRepository = new LocalMemoryCoRepository();
	private OriginalRepository originalRepository = new FakeOriginalRepository(new FakeVisitationDAO());

	@Test
	public void mulpipleClientsShouldShareOneCoRepositoryWithoutConflict() throws InterruptedException {
		final int clientCount = 100;
		final int callCount = 300;
		ExecutorService executors = Executors.newFixedThreadPool(clientCount);
		
		for (int i = 0; i < clientCount; i++) {
			clients.add(new WriteBackClient(coRepository, originalRepository));
			executors.submit(new OnlyIncreaseThread(key, clients.get(i), callCount));
		}
		executors.shutdown();
		executors.awaitTermination(5, TimeUnit.SECONDS);
		
		WriteBackItem result = clients.get(0).select(key);
		assertThat(result.getValueAsLong(), is((long)clientCount * callCount));
	}

	@Test
	public void mulpipleClientsCanIncreaseAndDecreaseOnSameKeyWithoutConflict() throws InterruptedException {
		final int clientCount = 100;
		final int callCount = 300;
		ExecutorService executors = Executors.newFixedThreadPool(clientCount);
		
		for (int i = 0; i < clientCount / 2; i++) {
			clients.add(new WriteBackClient(coRepository, originalRepository));
			executors.submit(new OnlyIncreaseThread(key, clients.get(i), callCount));
		}
		for (int i = clientCount / 2; i < clientCount; i++) {
			clients.add(new WriteBackClient(coRepository, originalRepository));
			executors.submit(new OnlyDecreaseThread(key, clients.get(i), callCount));
		}

		executors.shutdown();
		executors.awaitTermination(5, TimeUnit.SECONDS);
		
		WriteBackItem result = clients.get(0).select(key);
		assertThat(result.getValueAsLong(), is(0L));
	}
}
