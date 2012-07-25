package com.github.corepo.client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.corepo.client.support.FakeOriginalRepository;
import com.github.corepo.client.support.FakeVisitationDAO;

public class CoRepositoryEndToEndTest {
	private final String key = "count";
	@Test
	public void oneUserUsesCoRepository() {
		OriginalRepository originalRepository = new FakeOriginalRepository(
				new FakeVisitationDAO());
		CoRepositoryClient client = new CoRepositoryClient(
				new LocalMemoryCoRepository(), originalRepository, 10000);

		int count = client.selectAsInt(key).getValueAsInt();
		count++;
		count++;
		client.update(new Item(key, count));

		assertThat(client.selectAsInt(key).getValueAsInt(), is(2));
		assertThat(originalRepository.read(key).getValueAsInt(), is(0));
	}
}
