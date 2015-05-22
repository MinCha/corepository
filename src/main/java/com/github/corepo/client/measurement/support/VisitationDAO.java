package com.github.corepo.client.measurement.support;

import com.github.corepo.client.ItemKey;

import java.util.HashMap;
import java.util.Map;

public class VisitationDAO {
    private Map<ItemKey, Integer> table = new HashMap<ItemKey, Integer>();

    public int selectVisitationCount(ItemKey id) {
        Integer result = table.get(id);
        return result == null ? 0 : result;
    }

    public void updateVisitionCount(ItemKey key, int value) {
        table.put(key, value);
    }
}
