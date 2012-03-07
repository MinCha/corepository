package com.github.writeback.client;

import tokyotyrant.MRDB;
import tokyotyrant.transcoder.IntegerTranscoder;
import tokyotyrant.transcoder.StringTranscoder;

import com.github.writeback.client.exception.NonExistentKeyException;

public class TTCoRepository implements CoRepository {
	static final String LOCK_KEY_PREFIX = "_CO_REPOSITORY_LOCK_FOR_";
	private MRDB tt;

	private final StringTranscoder stringTranscoder = new StringTranscoder();
	private final IntegerTranscoder integerTranscoder = new IntegerTranscoder();

	public TTCoRepository(MRDB tt) {
		this.tt = tt;
	}

	public void update	(Item item) {
		assertThatThereIsKey(item.getKey());
		
		insert(item);
	}

	public void insert(Item item) {
		if (item.isInteger()) {
			tt.await(tt.put(item.getKey(), item.getValueAsInt(), integerTranscoder));			
		} else {
			tt.await(tt.put(item.getKey(), item.getValueAsString(), stringTranscoder));						
		}
	}

	public void increase(String key) {
		assertThatThereIsKey(key);
		
		tt.await(tt.addint(key, 1));
	}

	public void decrease(String key) {
		assertThatThereIsKey(key);
		tt.await(tt.addint(key, -1));
	}

	public boolean exists(String key) {
		return tt.await(tt.get(key, new StringTranscoder())) != null;
	}

	public boolean lock(String key) {
		return tt.await(tt.putkeep(LOCK_KEY_PREFIX + key, "locked"));
	}

	public boolean unlock(String key) {
		return tt.await(tt.out(LOCK_KEY_PREFIX + key));
	}

	public void delete(String key) {
		tt.await(tt.out(key));
	}

	private void assertThatThereIsKey(String key, Object value) {
		if (value == null) {
			throw new NonExistentKeyException(key);
		}
	}

	private void assertThatThereIsKey(String key) {
		assertThatThereIsKey(key, tt.await(tt.get(key, stringTranscoder)));
	}

	public Item selectAsString(String key) {
		Object value = tt.await(tt.get(key, stringTranscoder));
		assertThatThereIsKey(key, value);
		return new Item(key, (String) value);
	}

	public Item selectAsInt(String key) {
		Object value = tt.await(tt.get(key, integerTranscoder));
		assertThatThereIsKey(key, value);
		return new Item(key, (Integer)value);
	}
}
