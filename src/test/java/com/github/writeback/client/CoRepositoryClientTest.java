package com.github.writeback.client;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class CoRepositoryClientTest {
	private final Logger log = LoggerFactory.getLogger(CoRepositoryClientTest.class);
	
	private CoRepositoryClient sut;

	private final String key = "key";
	private final String noKey = "noKey";
	private final Item integerItem = new Item(key, 3);
	private final Item stringItem = new Item(key, "3");
	@Mock
	private CoRepository coRepository;
	@Mock
	private OriginalRepository originalRepository;

	@Before
	public void beforeEach() {
		when(coRepository.lock(key)).thenReturn(true);
		log.info("Message {} ", "Hi");
	}

	@Test
	public void canSelectStringValue() {
		when(originalRepository.read(key)).thenReturn(stringItem);
		when(coRepository.selectAsString(key)).thenReturn(stringItem);
		sut = new CoRepositoryClient(coRepository, originalRepository);

		Item result = sut.selectAsString(key);

		assertThat(result, is(stringItem));
	}

	@Test
	public void canSelectIntegerValue() {
		when(originalRepository.read(key)).thenReturn(integerItem);
		when(coRepository.selectAsInt(key)).thenReturn(integerItem);
		sut = new CoRepositoryClient(coRepository, originalRepository);

		Item result = sut.selectAsInt(key);

		assertThat(result, is(integerItem));
	}

	@Test
	public void canUpdateValue() {
		when(originalRepository.read(key)).thenReturn(integerItem);
		sut = new CoRepositoryClient(coRepository, originalRepository);

		sut.update(stringItem);

		verify(coRepository).update(stringItem);
	}

	@Test
	public void canIncreaseValue() {
		when(originalRepository.read(key)).thenReturn(integerItem);
		sut = new CoRepositoryClient(coRepository, originalRepository);

		sut.increase(key);

		verify(coRepository).increase(key);
	}

	@Test
	public void canDecreaseValue() {
		when(originalRepository.read(key)).thenReturn(integerItem);
		sut = new CoRepositoryClient(coRepository, originalRepository);

		sut.decrease(key);

		verify(coRepository).decrease(key);
	}

	@Test
	public void shouldThrowException_WhenOperatingWithNoKey() {
		when(coRepository.lock(noKey)).thenReturn(true);
		when(originalRepository.read(noKey))
				.thenReturn(Item.withNoValue(noKey));
		sut = new CoRepositoryClient(coRepository, originalRepository);

		try {
			when(coRepository.selectAsInt(noKey)).thenReturn(
					Item.withNoValue(noKey));
			sut.selectAsInt(noKey);
			fail();
		} catch (NonExistentKeyException e) {
		}

		try {
			sut.update(new Item(noKey, "anyValue"));
			fail();
		} catch (NonExistentKeyException e) {
		}

		try {
			sut.increase(noKey);
			fail();
		} catch (NonExistentKeyException e) {
		}

		try {
			sut.decrease(noKey);
			fail();
		} catch (NonExistentKeyException e) {
		}
	}
}
