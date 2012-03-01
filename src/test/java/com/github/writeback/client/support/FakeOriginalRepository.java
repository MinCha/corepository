package com.github.writeback.client.support;

import com.github.writeback.client.OriginalRepository;
import com.github.writeback.client.WriteBackItem;

public class FakeOriginalRepository implements OriginalRepository {
	private FakeVisitationDAO visitationDAO;
	
	public FakeOriginalRepository(FakeVisitationDAO visitationDAO) {
		this.visitationDAO = visitationDAO;
	}

	public WriteBackItem read(Object key) {
		long value = visitationDAO.selectVisitationCount((String) key);
		return new WriteBackItem(key, value);
	}

	public void writeBack(WriteBackItem item) {
		visitationDAO.updateVisitionCount(item.getKey(), item.getValueAsLong());
	}

}
