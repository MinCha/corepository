package com.github.writeback.client;

import java.util.List;

/**
 * This represents a repository which is holding intermediate values before write-back.
 * 
 * @author Min Cha
 *
 */
public interface CoRepository {
	WriteBackItem select(String key);
	void update(WriteBackItem item);
	void insert(WriteBackItem item);
	void increase(String key);
	void decrease(String key);
	boolean exists(String key);
	List<WriteBackItem> selectAll();
}
