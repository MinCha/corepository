package com.github.corepo.client;

/**
 * OriginalRepository, core domain object, is responsible for returning initial
 * values and write-backing intermediate values later.
 * 
 * If your original repository is a database, OriginalRepository 'read' method
 * will return the value of a row on table. Then, CoRepository handles UPDATE /
 * SELECT requests, using initial values from original repository. 'writeback'
 * method will be used when CoRepository writebacks changed initial values to
 * again original repository.
 * 
 * @author Min Cha
 */
public interface OriginalRepository {
	Item read(String key);

	void writeback(Item item);
}
