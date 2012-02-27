package com.github.writeback.client.support;

import com.github.writeback.client.OriginalRepository;
import com.github.writeback.client.WriteBackItem;

public class FakeOriginalRepository implements OriginalRepository {
	private FakeVisitationDAO visitationDAO;
	
	public FakeOriginalRepository(FakeVisitationDAO visitationDAO) {
		this.visitationDAO = visitationDAO;
	}

	public WriteBackItem read(String key) {
		long value = visitationDAO.selectVisitationCount(key);
		return new WriteBackItem(key, value);
	}

	public void writeBack(WriteBackItem item) {
		visitationDAO.updateVisitionCount(item.getKey(), item.getValueAsLong());
	}

}
