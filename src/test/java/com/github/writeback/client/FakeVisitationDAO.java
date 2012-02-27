package com.github.writeback.client;

import java.util.HashMap;
import java.util.Map;

public class FakeVisitationDAO {
	private Map<String, Long> table = new HashMap<String, Long>();

	public long selectVisitationCount(String id) {
		Long result = table.get(id);
		return result == null ? 1L : result;
	}

	public void updateVisitionCount(String key, long value) {
		table.put(key, value);
	}
}
