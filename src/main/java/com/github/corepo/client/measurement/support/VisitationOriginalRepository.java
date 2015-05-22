package com.github.corepo.client.measurement.support;

import com.github.corepo.client.Item;
import com.github.corepo.client.ItemKey;
import com.github.corepo.client.OriginalRepository;

import java.util.List;

public class VisitationOriginalRepository implements OriginalRepository {
    private VisitationDAO visitationDAO;

    public VisitationOriginalRepository(VisitationDAO visitationDAO) {
        this.visitationDAO = visitationDAO;
    }

    public Item read(ItemKey key) {
        int value = visitationDAO.selectVisitationCount(key);
        return new Item(key, value);
    }

    public void writeback(Item item) {
    }

    public void writeback(List<Item> items) {
        for (Item each : items) {
            visitationDAO.updateVisitionCount(each.getItemKey(),
                    each.getValueAsInt());
        }
    }
}
