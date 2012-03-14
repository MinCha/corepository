package com.github.writeback.client.support;

import com.github.writeback.client.OriginalRepository;
import com.github.writeback.client.Item;

public class FakeOriginalRepository implements OriginalRepository {
	private FakeVisitationDAO visitationDAO;
	
	public FakeOriginalRepository(FakeVisitationDAO visitationDAO) {
		this.visitationDAO = visitationDAO;
	}

	public Item read(String key) {
		int value = visitationDAO.selectVisitationCount((String) key);
		return new Item(key, value);
	}

	public void writeback(Item item) {
		visitationDAO.updateVisitionCount(item.getKey(), item.getValueAsInt());
	}

}
