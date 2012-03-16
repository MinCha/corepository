package com.github.corepo.client;

/**
 * CoRepository, core domain object, is responsible for receiving update and
 * select requests from client instead of original repository and sometimes
 * write-backing to original repository. For this reason, CoRepository should
 * able to rapidly process requests from client. You can read more at
 * https://github.com/MinCha/corepository/wiki
 * 
 * @see LocalMemoryCoRepository
 * @see TTCoRepository
 * @author Min Cha
 * 
 */
public interface CoRepository {
	Item selectAsString(String key);

	Item selectAsInt(String key);

	void update(Item item);

	void insert(Item item);

	int increase(String key);

	int decrease(String key);

	boolean exist(String key);

	void delete(String key);

	boolean lock(String key);

	boolean unlock(String key);
}
