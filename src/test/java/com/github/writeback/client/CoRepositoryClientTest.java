package com.github.writeback.client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.writeback.client.support.FakeOriginalRepository;
import com.github.writeback.client.support.FakeVisitationDAO;

public class CoRepositoryClientTest {
	private CoRepositoryClient sut;

	private final String key = "key";
	private CoRepository coRepository = new LocalMemoryCoRepository();
	private OriginalRepository originalRepository = new FakeOriginalRepository(new FakeVisitationDAO());

	@Test
	public void canSelectValue() {
		sut = new CoRepositoryClient(coRepository, originalRepository);
		
		Item result = sut.selectAsInt(key);

		assertThat(result, is(originalRepository.read(key)));
	}

	@Test
	public void canUpdateValue() {
		final String newValue = "some";
		sut = new CoRepositoryClient(coRepository, originalRepository);
		
		sut.update(new Item(key, newValue));
		
		assertThat(sut.selectAsString(key), is(new Item(key, newValue)));
	}

	@Test
	public void canIncreaseValue() {
		sut = new CoRepositoryClient(coRepository, originalRepository);
		int originalValue = sut.selectAsInt(key).getValueAsInt();
		
		sut.increase(key);
		
		assertThat(sut.selectAsInt(key).getValueAsInt(), is(originalValue + 1));		
	}

	@Test
	public void canDecreaseValue() {
		sut = new CoRepositoryClient(coRepository, originalRepository);
		int originalValue = sut.selectAsInt(key).getValueAsInt();
		
		sut.decrease(key);
		
		assertThat(sut.selectAsInt(key).getValueAsInt(), is(originalValue - 1));		
	}
}
