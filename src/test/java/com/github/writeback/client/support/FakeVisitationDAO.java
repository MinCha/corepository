package com.github.writeback.client.support;

import java.util.HashMap;
import java.util.Map;

public class FakeVisitationDAO {
	private Map<String, Long> table = new HashMap<String, Long>();

	public long selectVisitationCount(String id) {
		Long result = table.get(id);
		return result == null ? 0L : result;
	}

	public void updateVisitionCount(String key, long value) {
		table.put(key, value);
	}
}
