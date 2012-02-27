package com.github.writeback.client;

public interface OriginalRepository {
	WriteBackItem read(String key);
	void writeBack(WriteBackItem item);
}
