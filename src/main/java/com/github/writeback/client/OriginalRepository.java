package com.github.writeback.client;

/**
 * OriginalRepository, core domain object, is responsible for returning initial
 * values and write-back intermediate values later.
 * 
 * If your original repository is database, read method will return value of a
 * row on table, Then, CoRepository starts select and update operation based on
 * initial value from original repository. write back method will be used when
 * CoRepository want to write-back changed initial values to again original
 * repository.
 * 
 * @author Min Cha
 */
public interface OriginalRepository {
	Item read(String key);

	void writeBack(Item item);
}
