package com.github.corepo.client;

import tokyotyrant.MRDB;
import tokyotyrant.transcoder.IntegerTranscoder;
import tokyotyrant.transcoder.StringTranscoder;

public class TTCoRepository implements CoRepository {
	static final String LOCK_KEY_PREFIX = "_CO_REPOSITORY_LOCK_FOR_";
	private MRDB tt;

	private final StringTranscoder stringTranscoder = new StringTranscoder();
	private final IntegerTranscoder integerTranscoder = new IntegerTranscoder();

	public TTCoRepository(MRDB tt) {
		this.tt = tt;
	}

	public void update(Item item) {
		insert(item);
	}

	public void insert(Item item) {
		if (item.isInteger()) {
			tt.await(tt.put(item.getKey(), item.getValueAsInt(),
					integerTranscoder));
		} else {
			tt.await(tt.put(item.getKey(), item.getValueAsString(),
					stringTranscoder));
		}
	}

	public int increase(String key) {
		int result = tt.await(tt.addint(key, 1));
		return result;
	}

	public int decrease(String key) {
		int result = tt.await(tt.addint(key, -1));
		return result;
	}

	public boolean exist(String key) {
		return tt.await(tt.get(key, new StringTranscoder())) != null;
	}

	public boolean lock(String key) {
		final int winner = 1;
		int result = increase(TTCoRepository.LOCK_KEY_PREFIX + key);
		return winner == result;
	}

	public boolean unlock(String key) {
		return tt.await(tt.out(LOCK_KEY_PREFIX + key));
	}

	public void delete(String key) {
		tt.await(tt.out(key));
	}

	public Item selectAsString(String key) {
		Object value = tt.await(tt.get(key, stringTranscoder));
		if (value == null) {
			return Item.withNoValue(key);
		}
		return new Item(key, (String) value);						
	}

	public Item selectAsInt(String key) {
		Object value = tt.await(tt.get(key, integerTranscoder));
		if (value == null) {
			return Item.withNoValue(key);
		}
		return new Item(key, (Integer) value);						
	}
}
