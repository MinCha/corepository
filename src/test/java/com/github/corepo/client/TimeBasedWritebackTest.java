package com.github.corepo.client;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimeBasedWritebackTest {
    private TimeBasedWriteback sut;
    @Mock
    private Writeback writeback;
    @Mock
    private LRUKeyUpdateTime keyUpdateTime;
    private final Item item = new Item("string", "value");

    @Test
    public void shouldWritebackEveryPeriod() throws InterruptedException {
	when(keyUpdateTime.findKeysOverThan(10)).thenReturn(
		Arrays.asList(item.getKey()));
	sut = new TimeBasedWriteback(writeback, keyUpdateTime, 10);
	sut.start();

	Thread.sleep(10 * 5 + 10);
	sut.stop();
	verify(writeback, atLeast(5)).writeback(item.getKey());
    }
}
