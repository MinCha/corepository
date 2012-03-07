package com.github.writeback.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.writeback.client.exception.NonExistentKeyException;
import com.github.writeback.client.exception.NotNumericValueException;

/**
 * <code>CoRepository</code> using local memory.
 * 
 * If you use this class as CoRepository, you don`t need any remote
 * CoRepository.
 * 
 * <b>Warning!</b> In clustered environment such as web servers clustered by L4
 * switch, this class does not ensure that select method returns the
 * latest value.
 * 
 * @author Min Cha
 */
public class LocalMemoryCoRepository implements CoRepository {
	private Map<String, Object> locks = new ConcurrentHashMap<String, Object>();
	private Map<String, String> items = new ConcurrentHashMap<String, String>();
	private HashBasedMutexProvider mutex = new HashBasedMutexProvider();

	public void update(Item item) {
		assertThatThereIsKey(item.getKey());

		synchronized (mutex.get(item.getKey())) {
			items.put(item.getKey(), item.getValueAsString());
		}
	}

	public void increase(String key) {
		assertThatThereIsKey(key);

		synchronized (mutex.get(key)) {
			String result = items.get(key);
			long value = covertLongFrom(result);
			value++;
			items.put(key, String.valueOf(value));			
		}
	}

	public void decrease(String key) {
		assertThatThereIsKey(key);

		synchronized (mutex.get(key)) {
			String result = items.get(key);
			long value = covertLongFrom(result);
			value--;
	
			items.put(key, String.valueOf(value));
		}
	}

	public void insert(Item item) {
		synchronized (mutex.get(item.getKey())) {
			items.put(item.getKey(), item.getValueAsString());
		}
	}

	private void assertThatThereIsKey(String key) {
		if (items.containsKey(key) == false) {
			throw new NonExistentKeyException(key);
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
		assertThatThereIsKey(key);

		return new Item(key, items.get(key));
	}

	public Item selectAsInt(String key) {
		assertThatThereIsKey(key);

		return new Item(key, Integer.parseInt(items.get(key)));
	}
}
