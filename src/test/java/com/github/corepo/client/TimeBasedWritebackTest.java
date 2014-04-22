package com.github.corepo.client;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

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
    private WritebackEventNotifier notifier;
    @Mock
    private LRUKeyUpdateTime keyUpdateTime;

    @Test
    public void shouldWritebackEveryPeriod() throws InterruptedException {
	final long writebackPeriodInMillis = 10;
	sut = new TimeBasedWriteback(writeback, keyUpdateTime,
		writebackPeriodInMillis);
	sut.start();

	Thread.sleep(10 * 5 + 10);
	sut.stop();
	verify(keyUpdateTime, atLeast(5)).applyToKeysOverThan(
		writebackPeriodInMillis, writeback, true);
    }
}
