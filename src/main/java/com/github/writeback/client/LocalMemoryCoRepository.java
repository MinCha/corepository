package com.github.writeback.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <code>CoRepository</code> using local memory.
 * 
 * If you use this class as CoRepository, you don`t need any remote
 * CoRepository.
 * 
 * <b>Warning!</b> 
 * 1. In clustered environment such as web servers clustered by L4
 * switch, this class does not ensure that select method returns the latest
 * value.
 * 2. This CoRepository requires enough heap memory. Be careful of out of memory. 
 * 
 * @author Min Cha
 */
public class LocalMemoryCoRepository implements CoRepository {
	private Map<String, Object> locks = new ConcurrentHashMap<String, Object>();
	private Map<String, String> items = new ConcurrentHashMap<String, String>();
	private HashBasedMutexProvider mutex = new HashBasedMutexProvider();
	private static final String META_PREFIX = "_META_";  

	public void update(Item item) {
		synchronized (mutex.get(item.getKey())) {
			items.put(item.getKey(), item.getValueAsString());
			updateMeta(item.getKey());
		}
	}

	public void increase(String key) {
		synchronized (mutex.get(key)) {
			String result = items.get(key);
			long value = covertLongFrom(result);
			value++;
			items.put(key, String.valueOf(value));
			updateMeta(key);
		}
	}

	public void decrease(String key) {
		synchronized (mutex.get(key)) {
			String result = items.get(key);
			long value = covertLongFrom(result);
			value--;

			items.put(key, String.valueOf(value));
			updateMeta(key);
		}
	}

	public void insert(Item item) {
		synchronized (mutex.get(item.getKey())) {
			items.put(item.getKey(), item.getValueAsString());
		}
	}

	public boolean exists(String key) {
		return items.containsKey(key);
	}

	private long covertLongFrom(String result) {
		long value;
		try {
			value = Long.parseLong(result);
		} catch (Exception e) {
			throw new NotNumericValueException(result, e);
		}
		return value;
	}

	public boolean lock(String key) {
		if (locks.containsKey(key)) {
			return false;
		} else {
			locks.put(key, new Object());
			return true;
		}
	}

	public boolean unlock(String key) {
		if (locks.containsKey(key)) {
			locks.remove(key);
			return true;
		} else {
			return false;
		}
	}

	public void delete(String key) {
		items.remove(key);
	}

	public Item selectAsString(String key) {
		if (items.containsKey(key) == false) {
			return Item.withNoValue(key);
		}
		
		String value = items.get(key);
		if (items.containsKey(META_PREFIX + key)) {
			String meta = items.get(META_PREFIX + key);
			long lastUpdatedTime = Long.parseLong(meta.split("-")[0]);
			long lastWritebackedTime = Long.parseLong(meta.split("-")[1]);
			return new Item(key, value, lastUpdatedTime, lastWritebackedTime);
		} else {
			return new Item(key, value);			
		}
	}

	public Item selectAsInt(String key) {
		if (items.containsKey(key)) {
			return new Item(key, Integer.parseInt(items.get(key)));
		} else {
			return Item.withNoValue(key);
		}
	}

	private void updateMeta(String key) {
		items.put(META_PREFIX + key, System.currentTimeMillis() + "-" + "0");
	}
}
