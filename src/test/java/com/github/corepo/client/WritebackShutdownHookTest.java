package com.github.corepo.client;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WritebackShutdownHookTest {
	private WritebackShutdownHook sut;
	@Mock
	private LRUKeyUpdateTime keyUpdateTime;
	private List<String> items = Arrays.asList("a", "b"); 
	
	@Test
	public void allPendingItemsShouldBeWritebacked() {
		sut = new WritebackShutdownHook(keyUpdateTime);
		when(keyUpdateTime.findKeysOverThan(0)).thenReturn(items);
		
		sut.run();
		
		verify(keyUpdateTime).removeAll();
	}
}
