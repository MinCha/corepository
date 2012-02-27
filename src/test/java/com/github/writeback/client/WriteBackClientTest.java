package com.github.writeback.client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.writeback.client.support.FakeOriginalRepository;

public class WriteBackClientTest {
	private WriteBackClient sut;

	private final String key = "key";
	private CoRepository coRepository = new LocalMemoryCoRepository();
	private OriginalRepository originalRepository = new FakeOriginalRepository(new FakeVisitationDAO());

	@Test
	public void canSelectValue() {
		sut = new WriteBackClient(coRepository, originalRepository);
		
		WriteBackItem result = sut.select(key);

		assertThat(result, is(originalRepository.read(key)));
	}

	@Test
	public void canUpdateValue() {
		final String newValue = "some";
		sut = new WriteBackClient(coRepository, originalRepository);
		
		sut.update(new WriteBackItem(key, newValue));
		
		assertThat(sut.select(key), is(new WriteBackItem(key, newValue)));
	}

	@Test
	public void canIncreaseValue() {
		sut = new WriteBackClient(coRepository, originalRepository);
		long originalValue = (Long)sut.select(key).getValue();
		
		sut.increase(key);
		
		assertThat(sut.select(key).getValueAsLong(), is(originalValue + 1));		
	}

	@Test
	public void canDecreaseValue() {
		sut = new WriteBackClient(coRepository, originalRepository);
		long originalValue = (Long)sut.select(key).getValue();
		
		sut.decrease(key);
		
		assertThat(sut.select(key).getValueAsLong(), is(originalValue - 1));		
	}
}
