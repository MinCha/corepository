package com.github.corepo.client;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

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
    public void canReturnAllKeysTimeOvered() {
	sut = new LRUKeyUpdateTime(removalListener);
	final long current = System.currentTimeMillis();
	sut.notifyUpdated("a", secondsAgo(2));
	sut.notifyUpdated("b", secondsAgo(2));
	sut.notifyUpdated("c", secondsAgo(2));
	sut.notifyUpdated("d", current);
	sut.notifyWritebacked("c", current);

	List<String> result = sut.findKeysOverThan(1000);

	assertThat(result.size(), is(2));
	assertThat(result, hasItem("a"));
	assertThat(result, hasItem("b"));
    }

    private long secondsAgo(int seconds) {
	return System.currentTimeMillis() - (1000 * seconds);
    }

    @Test
    public void canNotReturnKeysTimeOvered_NotChanged() {
	sut = new LRUKeyUpdateTime(removalListener);
	sut.notifyUpdated("a", secondsAgo(65));
	sut.notifyWritebacked("a", secondsAgo(64));

	List<String> result = sut.findKeysOverThan(1000 * 60);

	assertThat(result.size(), is(0));
    }
}
