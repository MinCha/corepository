package com.github.corepo.client.measurement.support;

import com.github.corepo.client.Item;
import com.github.corepo.client.OriginalRepository;

public class NagativeOriginalRepository implements OriginalRepository {
	public Item read(String key) {
		return Item.withNoValue(key);
	}

	public void writeback(Item item) {
	}
}
