package com.github.corepo.client;

/**
 * CoRepository, core domain object, is responsible for handling UPDATE / SELECT
 * requests from client instead of original repository. For this reason,
 * CoRepository should able to rapidly handle requests from client. You can read
 * more at https://github.com/MinCha/corepository/wiki
 *
 * @author Min Cha
 * @see LocalMemoryCoRepository
 * @see TTCoRepository
 * @see RedisCoRepository
 * @see CoRepositoryClient
 */
public interface CoRepository {
    Item selectAsObject(ItemKey key);

    Item selectAsInt(ItemKey key);

    void update(Item item);

    void insert(Item item);

    int increase(ItemKey key);

    int decrease(ItemKey key);

    boolean exists(ItemKey key);

    boolean delete(ItemKey key);

    boolean lock(ItemKey key);

    boolean unlock(ItemKey key);

    boolean isInt(ItemKey key);

    boolean isConnected();

    void close();
}
