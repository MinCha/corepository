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
		updateMeta(item.getKey());
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
		updateMeta(key);
		return result;
	}

	public int decrease(String key) {
		int result = tt.await(tt.addint(key, -1));
		updateMeta(key);
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
		tt.await(tt.out(Item.META_PREFIX + key));
	}

	public Item selectAsString(String key) {
		Object value = tt.await(tt.get(key, stringTranscoder));
		if (value == null) {
			return Item.withNoValue(key);
		}
		
		Object meta = tt.await(tt.get(Item.META_PREFIX + key, stringTranscoder));
		if (meta == null) {
			return new Item(key, (String) value);						
		} else {
			long[] updateTimes = extractUpdatedTimes((String) meta);
			return new Item(key, (String)value, updateTimes[0], updateTimes[1]);
		} 
	}

	public Item selectAsInt(String key) {
		Object value = tt.await(tt.get(key, integerTranscoder));
		if (value == null) {
			return Item.withNoValue(key);
		}
		
		Object meta = tt.await(tt.get(Item.META_PREFIX + key, stringTranscoder));
		if (meta == null) {
			return new Item(key, (Integer) value);						
		} else {
			long[] updateTimes = extractUpdatedTimes((String) meta);
			return new Item(key, (Integer)value, updateTimes[0], updateTimes[1]);
		} 
	}

	private void updateMeta(String key) {
		tt.await(tt.put(Item.META_PREFIX + key, System.currentTimeMillis() + "-" + Item.NO_WRITEBACKED));
	}
	
	private long[] extractUpdatedTimes(String meta) {
		meta = meta.trim();
		long lastUpdatedTime = Long.parseLong(meta.split("-")[0]);
		long lastWritebackedTime = Long.parseLong(meta.split("-")[1]);
		return new long[]{lastUpdatedTime, lastWritebackedTime};
	}
}
