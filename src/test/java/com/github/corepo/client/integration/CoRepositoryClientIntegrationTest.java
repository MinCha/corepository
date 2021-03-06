package com.github.corepo.client.integration;

import com.github.corepo.client.*;
import com.github.corepo.client.measurement.support.NagativeOriginalRepository;
import com.github.corepo.client.measurement.support.PossitiveOriginalRepository;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CoRepositoryClientIntegrationTest {
    private LocalMemoryCoRepository localMemory = new LocalMemoryCoRepository();
    private CoRepositoryClient sut;
    private int nonExistenceKey = 0;
    private int timeout = 0;

    @MultiThreadTest
    @Test
    public void watingThreadsShouldReciveTimeoutException_WhenThereIsNoKeyOnOriginalRepository() {
        sut = new CoRepositoryClient(localMemory,
                new NagativeOriginalRepository());
        final ItemKey key = new ItemKey("non-existing");
        final int clientCount = 500;
        ExecutorService executors = Executors.newFixedThreadPool(1000);
        List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
        try {
            sut.increase(key);
        } catch (NonExistentKeyException e) {
            nonExistenceKey++;
        }
        for (int i = 0; i < clientCount - 1; i++) {
            tasks.add(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    try {
                        sut.increase(key);
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
            executors.awaitTermination(60, TimeUnit.SECONDS);
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
            sut.update(new Item(new ItemKey("K" + i), 0));
        }

        sut.close();

        assertThat(localMemory.size(), is(0));
    }
}
