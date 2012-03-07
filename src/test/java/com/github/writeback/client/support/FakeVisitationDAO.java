package com.github.writeback.client.support;

import java.util.HashMap;
import java.util.Map;

public class FakeVisitationDAO {
	private Map<String, Integer> table = new HashMap<String, Integer>();

	public int selectVisitationCount(String id) {
		Integer result = table.get(id);
		return result == null ? 0 : result;
	}

	public void updateVisitionCount(String key, int value) {
		table.put((String) key, value);
	}
}
