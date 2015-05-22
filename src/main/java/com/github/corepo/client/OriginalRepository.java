package com.github.corepo.client;

import java.util.List;

/**
 * OriginalRepository, core domain object, is responsible for returning initial
 * values and write-backing intermediate values again.
 * <p/>
 * If your original repository is a database OriginalRepository, for example,
 * 'read' method will return the value of a row on table. After this,
 * CoRepository can handle UPDATE / SELECT requests. On the other hands,
 * 'writeback' method will be used when CoRepository write-backs changed initial
 * values to again original repository.
 *
 * @author Min Cha
 */
public interface OriginalRepository {
    Item read(String key);

    void writeback(List<Item> items);
}
