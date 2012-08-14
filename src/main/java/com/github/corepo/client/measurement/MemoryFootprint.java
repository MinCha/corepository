package com.github.corepo.client.measurement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.corepo.client.CoRepository;
import com.github.corepo.client.CoRepositoryClient;
import com.github.corepo.client.Item;
import com.github.corepo.client.TTCoRepository;
import com.github.corepo.client.measurement.support.PossitiveOriginalRepository;

public class MemoryFootprint {
	private static AtomicInteger keyIndex = new AtomicInteger();;

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		CoRepository coRepository = new TTCoRepository("10.64.135.189", 1978);
		final CoRepositoryClient client = new CoRepositoryClient(coRepository,
				new PossitiveOriginalRepository(), 1000 * 60);
		final CoRepositoryClient secondClient = new CoRepositoryClient(
				coRepository, new PossitiveOriginalRepository(), 1000 * 60);
		final CoRepositoryClient thirdClient = new CoRepositoryClient(
				coRepository, new PossitiveOriginalRepository(), 1000 * 60);

		ExecutorService executors = Executors.newFixedThreadPool(100);
		List<Callable<Boolean>> tasks = createMemoryConsumer(client);
		executors.invokeAll(tasks);
		System.out.println("Step1 Completed.");

		tasks = createMemoryConsumer(secondClient);
		executors.invokeAll(tasks);
		System.out.println("Step2 Completed.");

		tasks = createMemoryConsumer(thirdClient);
		executors.invokeAll(tasks);
		System.out.println("Step3 Completed.");

		executors.shutdown();
		System.out.println("This will be closed.");
		client.close();

		System.out.println(System.currentTimeMillis() - start);
	}

	private static List<Callable<Boolean>> createMemoryConsumer(
			final CoRepositoryClient client) {
		List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
		for (int i = 0; i < 1000; i++) {
			tasks.add(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					for (int j = 0; j < 100; j++) {
						client.update(new Item("K" + keyIndex.addAndGet(1), 0));
					}
					return Boolean.TRUE;
				}
			});
		}
		return tasks;
	}
}
