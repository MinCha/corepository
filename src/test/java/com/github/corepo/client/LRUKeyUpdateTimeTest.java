package com.github.corepo.client;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

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
			public void onRemoval(RemovalNotification<String, UpdateTime> notification) {
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
		
		verify(removalListener).onRemoval(Mockito.any(RemovalNotification.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldFireRemovalEvent_WhenRemovingAllItems() {
		sut = new LRUKeyUpdateTime(removalListener, 2);
		sut.notifyUpdated(keyA, System.currentTimeMillis());
		sut.notifyUpdated(keyB, System.currentTimeMillis());
		sut.notifyUpdated(keyA, System.currentTimeMillis());
		sut.notifyUpdated(keyB, System.currentTimeMillis());

		sut.removeAll();
		
		verify(removalListener, times(2)).onRemoval(Mockito.any(RemovalNotification.class));
	}

	@Test
	public void canReturnAllKeysTimeOvered() {
		sut = new LRUKeyUpdateTime(removalListener);
		final long current = System.currentTimeMillis();
		final long timeInMillis = 1000 * 60 * 1;
		final long extra = 100;
		sut.notifyUpdated("a", current - timeInMillis - extra);
		sut.notifyUpdated("b", current - timeInMillis - extra);
		sut.notifyUpdated("c", current - timeInMillis - extra);
		sut.notifyUpdated("d", current);
		sut.notifyWritebacked("c", current);
		
		List<String> result = sut.findKeysOverThan(timeInMillis);
		
		assertThat(result.size(), is(2));
		assertThat(result, hasItem("a"));
		assertThat(result, hasItem("b"));
	}
}
