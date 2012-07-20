package com.github.corepo.client;

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
 * switch, this class does not ensure that select method returns the recent
 * value.
 * 2. LocalMemoryCoRepository requires enough heap memory. Be careful of out of memory. 
 * 
 * @author Min Cha
 */
public class LocalMemoryCoRepository implements CoRepository {
	private Map<String, Object> locks = new ConcurrentHashMap<String, Object>();
	private Map<String, String> items = new ConcurrentHashMap<String, String>();
	private HashBasedMutexProvider mutex = new HashBasedMutexProvider();

	public void update(Item item) {
		synchronized (mutex.get(item.getKey())) {
			items.put(item.getKey(), item.getValueAsString());
		}
	}

	public int increase(String key) {
		synchronized (mutex.get(key)) {
			String result = items.get(key);
			int value = convertIntFrom(result);
			value++;
			items.put(key, String.valueOf(value));
			return value;
		}
	}

	public int decrease(String key) {
		synchronized (mutex.get(key)) {
			String result = items.get(key);
			int value = convertIntFrom(result);
			value--;
			items.put(key, String.valueOf(value));
			return value;
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

	private int convertIntFrom(String result) {
		int value;
		try {
			value = Integer.parseInt(result);
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

	public boolean delete(String key) {
		return items.remove(key) != null;
	}

	public Item selectAsString(String key) {
		if (items.containsKey(key) == false) {
			return Item.withNoValue(key);
		}
		
		String value = items.get(key);
		return new Item(key, value);			
	}

	public Item selectAsInt(String key) {
		if (items.containsKey(key) == false) {
			return Item.withNoValue(key);
		}

		int value = Integer.parseInt(items.get(key));
		return new Item(key, value);			
	}

	public boolean isInt(String key) {
		try {
			selectAsInt(key);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
