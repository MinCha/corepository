package com.github.writeback.client;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.github.writeback.client.WriteBackItem;
import com.github.writeback.client.exception.IllegalKeyException;

public class WriteBackItemTest {
	@SuppressWarnings("unused")
	private WriteBackItem sut;
	
	@Test
	public void maximumLengthOfKeyShouldBeLimited() {
		try {
			sut = new WriteBackItem(overKey(), "value");
			fail();
		} catch (IllegalKeyException e) {
		}
	}

	private String overKey() {
		final StringBuffer result = new StringBuffer();
		for (int i = 0 ; i < WriteBackItem.MAXIMUM_KEY_LENGTH + 1; i++) {
			result.append("k");
		}
		return result.toString();
	}
}
