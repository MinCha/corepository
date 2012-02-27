package com.github.writeback.client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.writeback.client.support.FakeOriginalRepository;
import com.github.writeback.client.support.FakeVisitationDAO;

public class PeriodicWriteBackTest {
	private PeriodicWriteBack sut;

	private final String key = "key";
	private CoRepository coRepository = new LocalMemoryCoRepository();
	private OriginalRepository originalRepository = new FakeOriginalRepository(
			new FakeVisitationDAO());

	@Test
	public void shouldWriteBackToOriginalRepositoryPeriodically() throws InterruptedException {
		final long writeBackPeriodInMills = 10;
		sut = new PeriodicWriteBack(coRepository, originalRepository, writeBackPeriodInMills);
		final long value = 10L;
		coRepository.insert(new WriteBackItem(key, "1L"));
		coRepository.update(new WriteBackItem(key, value));
		
		sut.start();

		Thread.sleep(writeBackPeriodInMills * 2);
		WriteBackItem result = originalRepository.read(key);
		assertThat(result.getValueAsLong(), is(value));
	}

	@Test
	public void shouldTerminateThreadWithJVM() throws InterruptedException {
		sut = new PeriodicWriteBack(coRepository, originalRepository, 10);
		sut.start();
	}
}
