package com.github.corepo.client;

/**
 * CoRepository, core domain object, is responsible for handling UPDATE / SELECT
 * requests from client instead of original repository. For this reason,
 * CoRepository should able to rapidly handle requests from client. You can read
 * more at https://github.com/MinCha/corepository/wiki
 * 
 * @see LocalMemoryCoRepository
 * @see TTCoRepository
 * @see RedisCoRepository
 * @see CoRepositoryClient
 * @author Min Cha
 * 
 */
public interface CoRepository {
	Item selectAsObject(String key);

	Item selectAsInt(String key);

	void update(Item item);

	void insert(Item item);

	int increase(String key);

	int decrease(String key);

	boolean exists(String key);

	boolean delete(String key);

	boolean lock(String key);

	boolean unlock(String key);

	boolean isInt(String key);

	boolean isConnected();

	void close();
}
