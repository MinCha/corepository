package com.github.writeback.client;


/**
 * CoRepository, core domain object, is responsible for receiving update and
 * select requests from client instead of original repository such as database
 * and sometimes write-backing to original repository. For this reason,
 * CoRepository should able to rapidly process requests from client. You can
 * read more at https://github.com/MinCha/write-back/wiki
 * 
 * @see LocalMemoryCoRepository
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

	boolean exists(String key);

	void delete(String key);

	boolean lock(String key);
	
	boolean unlock(String key);
}
