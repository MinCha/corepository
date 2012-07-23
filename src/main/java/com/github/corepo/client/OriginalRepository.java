package com.github.corepo.client;

/**
 * OriginalRepository, core domain object, is responsible for returning initial
 * values and write-backing intermediate values later.
 * 
 * If your original repository is a database, read method will return value of a
 * row on table, Then, CoRepository starts select and update operation based on
 * initial value from original repository. 'writeback' method will be used when
 * CoRepository want to write-back changed initial values to again original
 * repository.
 * 
 * @author Min Cha
 */
public interface OriginalRepository {
	Item read(String key);

	void writeback(Item item);
}
