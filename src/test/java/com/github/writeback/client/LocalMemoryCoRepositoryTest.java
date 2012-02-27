package com.github.writeback.client;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.github.writeback.client.LocalMemoryCoRepository;
import com.github.writeback.client.WriteBackItem;
import com.github.writeback.client.exception.NonexistentKeyException;
import com.github.writeback.client.exception.NotNumericValueException;

public class LocalMemoryCoRepositoryTest {
	private LocalMemoryCoRepository sut = new LocalMemoryCoRepository();
	final String key = "visitCount-1";
	final String noKey = "noKey";

	@Test
	public void canSelectValue() {
		final long value = 3;
		sut.insert(new WriteBackItem(key, value));
		
		WriteBackItem result = sut.select(key);
		
		assertThat(result, is(new WriteBackItem(key, value)));
	}
	
	@Test(expected=NonexistentKeyException.class)
	public void shouldThrowException_WhenSelectingValueWithNoKey() {
		sut.select(noKey);
	}
	
	@Test
	public void canUpdateValue() {
		final long value = 3;
		final long newValue = 5;
		sut.insert(new WriteBackItem(key, value));
		
		sut.update(new WriteBackItem(key, newValue));
		
		assertThat(sut.select(key), is(new WriteBackItem(key, newValue)));		
	}

	@Test(expected=NonexistentKeyException.class)
	public void shouldThrowException_WhenUpdatingValueWithNoKey() {
		final Object anyValue = new Object();
		
		sut.update(new WriteBackItem(noKey, anyValue));
	}
	
	@Test
	public void canIncreaseValue() {
		final long value = 3;
		sut.insert(new WriteBackItem(key, value));
		
		sut.increase(key);
		
		assertThat(sut.select(key), is(new WriteBackItem(key, value + 1)));		
	}

	@Test(expected=NonexistentKeyException.class)
	public void shouldThrowException_WhenIncreasingValueWithNoKey() {
		sut.increase(noKey);
	}

	@Test(expected=NotNumericValueException.class)
	public void shouldThrowException_WhenIncreasingNotNumericValue() {
		final String stringValue = "high";
		sut.insert(new WriteBackItem(key, stringValue));
		
		sut.increase(key);
	}

	@Test
	public void canDecreaseValue() {
		final long value = 3;
		sut.insert(new WriteBackItem(key, value));
		
		sut.decrease(key);
		
		assertThat(sut.select(key), is(new WriteBackItem(key, value - 1)));		
	}

	@Test(expected=NonexistentKeyException.class)
	public void shouldThrowException_WhenDecreasingValueWithNoKey() {
		sut.decrease(noKey);
	}

	@Test(expected=NotNumericValueException.class)
	public void shouldThrowException_WhenDecreasingNotNumericValue() {
		final String stringValue = "high";
		sut.insert(new WriteBackItem(key, stringValue));
		
		sut.decrease(key);
	}
	
	@Test
	public void canKnowWhetherThereIsKey() {
		sut.insert(new WriteBackItem(key, new Object()));
		
		assertThat(sut.exists(key), is(true));
		assertThat(sut.exists(noKey), is(false));
	}

	@Test
	public void canObtainAllItems() {
		sut.insert(new WriteBackItem("1", 1));
		sut.insert(new WriteBackItem("2", 1));
		sut.insert(new WriteBackItem("3", 1));
		sut.update(new WriteBackItem("2", 1));
		
		List<WriteBackItem> result = sut.selectAll();
		
		assertThat(result.size(), is(3));
		assertThat(result, hasItem(new WriteBackItem("1", 1)));
		assertThat(result, hasItem(new WriteBackItem("2", 1)));
		assertThat(result, hasItem(new WriteBackItem("3", 1)));
	}
	
	@Test
	public void canSaveAtLeastOneMillionItems() {
		for (int i = 0; i < 1000000; i++) {
			sut.insert(new WriteBackItem("domain key is " + i, i));
		}
	}
}
