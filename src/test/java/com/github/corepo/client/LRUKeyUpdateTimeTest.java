package com.github.corepo.client;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

@RunWith(MockitoJUnitRunner.class)
public class LRUKeyUpdateTimeTest {
    private LRUKeyUpdateTime sut;
    private final String keyA = "keyA";
    private final String keyB = "keyB";
    private final String nokey = "nokey";
    @Mock
    private RemovalListener<String, UpdateTime> removalListener;
    @Mock
    private Writeback writeback;
    private int count = 0;

    @Test
    public void removalEventShouldOccurWhenOnlyRemovingItem() {
	sut = new LRUKeyUpdateTime(new RemovalListener<String, UpdateTime>() {
	    public void onRemoval(
		    RemovalNotification<String, UpdateTime> notification) {
		count++;
	    }
	});

	sut.notifyUpdated(keyA, System.currentTimeMillis());
	sut.notifyUpdated(keyA, System.currentTimeMillis());
	sut.notifyUpdated(keyA, System.currentTimeMillis());

	assertThat(count, is(0));
    }

    @Test
    public void canUpdateLastUpdatedTime() {
	sut = new LRUKeyUpdateTime(removalListener);
	long lastUpdatedTime = System.currentTimeMillis();

	sut.notifyUpdated(keyA, lastUpdatedTime);

	assertThat(sut.isUpdated(keyA), is(true));
	assertThat(sut.isUpdated(nokey), is(false));
    }

    @Test
    public void canUpdateLastWritebackedTime() {
	sut = new LRUKeyUpdateTime(removalListener);
	long lastWritebackedTime = System.currentTimeMillis();

	sut.notifyWritebacked(keyA, lastWritebackedTime);

	assertThat(sut.isWritebacked(keyA), is(true));
	assertThat(sut.isWritebacked(nokey), is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldFireRemovalEvent_WhenSizeIsOver() {
	sut = new LRUKeyUpdateTime(removalListener, 1);
	sut.notifyUpdated(keyA, System.currentTimeMillis());

	sut.notifyUpdated(keyB, System.currentTimeMillis());

	verify(removalListener).onRemoval(
		Mockito.any(RemovalNotification.class));
    }

    @Test
    public void shouldReturnAllKeys() {
	sut = new LRUKeyUpdateTime(removalListener, 2);
	sut.notifyUpdated(keyA, System.currentTimeMillis());
	sut.notifyUpdated(keyB, System.currentTimeMillis());
	sut.notifyUpdated(keyA, System.currentTimeMillis());
	sut.notifyUpdated(keyB, System.currentTimeMillis());

	Set<String> result = sut.findAllKeys();

	assertThat(result.size(), is(2));
	assertThat(result, hasItem(keyA));
	assertThat(result, hasItem(keyB));
    }

    @Test
    public void canApplyFunctionToKeysTimeOvered() {
	sut = new LRUKeyUpdateTime(removalListener);
	sut.notifyUpdated("a", secondsAgo(3));
	sut.notifyUpdated("b", secondsAgo(3));
	sut.notifyUpdated("b", secondsAgo(1));

	sut.applyToKeysOverThan(2000, writeback, false);

	verify(writeback).execute("a");
	verify(writeback).execute("b");
    }

    @Test
    public void shouldNotApplyFunctionWhenNotChangedAfterWritebacking() {
	sut = new LRUKeyUpdateTime(removalListener);
	sut.notifyUpdated("a", secondsAgo(65));
	sut.notifyWritebacked("a", secondsAgo(64));

	sut.applyToKeysOverThan(1000 * 60, writeback, false);

	verifyZeroInteractions(writeback);
    }

    @MultiThreadTest
    @Test
    public void findingShouldBeSafeWhenOtherThreadsUpdateMap()
	    throws InterruptedException {
	final int notifierCount = 10;
	final int writebackerCount = 1;
	sut = new LRUKeyUpdateTime(removalListener);

	ExecutorService executors = Executors.newFixedThreadPool(notifierCount
		+ writebackerCount);
	for (int i = 0; i < notifierCount; i++) {
	    executors.submit(new Runnable() {
		public void run() {
		    for (int i = 0; i < 5000; i++) {
			sut.notifyUpdated("K" + new Random().nextInt(),
				System.currentTimeMillis());
		    }
		}
	    });
	}

	executors.submit(new Runnable() {
	    public void run() {
		for (int i = 0; i < 100; i++) {
		    sut.applyToKeysOverThan(0, writeback, false);
		}
	    }
	});

	executors.shutdown();
	executors.awaitTermination(60, TimeUnit.SECONDS);
    }

    private long secondsAgo(int seconds) {
	return System.currentTimeMillis() - (1000 * seconds);
    }
}
