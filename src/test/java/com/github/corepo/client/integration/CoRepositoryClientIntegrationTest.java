package com.github.corepo.client.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.corepo.client.CoRepositoryClient;
import com.github.corepo.client.Item;
import com.github.corepo.client.LocalMemoryCoRepository;
import com.github.corepo.client.NonExistentKeyException;
import com.github.corepo.client.TimeoutException;
import com.github.corepo.client.measurement.support.NagativeOriginalRepository;
import com.github.corepo.client.measurement.support.PossitiveOriginalRepository;

public class CoRepositoryClientIntegrationTest {
    private LocalMemoryCoRepository localMemory = new LocalMemoryCoRepository();
    private CoRepositoryClient sut;
    private int nonExistenceKey = 0;
    private int timeout = 0;

    @Test
    public void watingThreadsShouldReciveTimeoutException_WhenThereIsNoKeyOnOriginalRepository() {
	sut = new CoRepositoryClient(localMemory,
		new NagativeOriginalRepository());
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
	    executors.shutdown();
	    executors.awaitTermination(5, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
	}

	assertThat(nonExistenceKey, is(1));
	assertThat(timeout, is(clientCount - 1));
    }

    @Test
    public void allOfItemsShouldBeCleared_WhenExit()
	    throws InterruptedException {
	sut = new CoRepositoryClient(localMemory,
		new PossitiveOriginalRepository());
	for (int i = 0; i < 100000; i++) {
	    sut.update(new Item("K" + i, 0));
	}

	sut.close();

	assertThat(localMemory.size(), is(0));
    }
}
