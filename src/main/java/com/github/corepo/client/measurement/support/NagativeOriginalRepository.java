package com.github.corepo.client.measurement.support;

import com.github.corepo.client.Item;
import com.github.corepo.client.ItemKey;
import com.github.corepo.client.OriginalRepository;

import java.util.List;

public class NagativeOriginalRepository implements OriginalRepository {
    public Item read(ItemKey key) {
        return Item.withNoValue(key);
    }

    public void writeback(List<Item> item) {
    }
}
