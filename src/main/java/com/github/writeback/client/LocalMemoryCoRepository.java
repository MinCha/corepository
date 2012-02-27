package com.github.writeback.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.writeback.client.exception.NonexistentKeyException;
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
	private Object mutex = new Object();
	private Map<String, Object> items = new ConcurrentHashMap<String, Object>();

	public WriteBackItem select(String key) {
		assertThatThereIsKey(key);

		return new WriteBackItem(key, items.get(key));
	}

	public void update(WriteBackItem item) {
		assertThatThereIsKey(item.getKey());

		synchronized (mutex) {
			items.put(item.getKey(), item.getValue());
		}
	}

	public void increase(String key) {
		assertThatThereIsKey(key);

		synchronized (mutex) {
			Object result = items.get(key);
			long value = covertLongFrom(result);
			value++;
			items.put(key, value);			
		}
	}

	public void decrease(String key) {
		assertThatThereIsKey(key);

		synchronized (mutex) {
			Object result = items.get(key);
			long value = covertLongFrom(result);
			value--;
	
			items.put(key, value);
		}
	}

	public void insert(WriteBackItem item) {
		items.put(item.getKey(), item.getValue());
	}

	private void assertThatThereIsKey(String key) {
		if (items.containsKey(key) == false) {
			throw new NonexistentKeyException("Key does not exist : " + key);
		}
	}

	public boolean exists(String key) {
		return items.containsKey(key);
	}

	public List<WriteBackItem> selectAll() {
		List<WriteBackItem> result = new ArrayList<WriteBackItem>();

		for (String each : items.keySet()) {
			result.add(new WriteBackItem(each, items.get(each)));
		}

		return result;
	}

	private long covertLongFrom(Object result) {
		long value;
		try {
			value = (Long) result;
		} catch (ClassCastException e) {
			throw new NotNumericValueException("Cannot increase value : "
					+ result);
		}
		return value;
	}

	public synchronized boolean lock(String key) {
		if (locks.containsKey(key)) {
			return false;
		} else {
			locks.put(key, new Object());
			return true;
		}
	}

	public synchronized boolean unlock(String key) {
		if (locks.containsKey(key)) {
			locks.remove(key);
			return true;
		} else {
			return false;
		}
	}
}
