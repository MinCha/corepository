package com.github.corepo.client.measurement.support;

import java.util.List;

import com.github.corepo.client.Item;
import com.github.corepo.client.OriginalRepository;

public class VisitationOriginalRepository implements OriginalRepository {
	private VisitationDAO visitationDAO;

	public VisitationOriginalRepository(VisitationDAO visitationDAO) {
		this.visitationDAO = visitationDAO;
	}

	public Item read(String key) {
		int value = visitationDAO.selectVisitationCount((String) key);
		return new Item(key, value);
	}

	public void writeback(Item item) {
	}

	public void writeback(List<Item> items) {
		for (Item each : items) {
			visitationDAO.updateVisitionCount(each.getKey(),
					each.getValueAsInt());
		}
	}
}
