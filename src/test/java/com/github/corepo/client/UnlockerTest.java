package com.github.corepo.client;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UnlockerTest {
	private Unlocker sut;
	private String key = "key";
	@Mock
	private CoRepository coRepository;

	@Test
	public void shouldUnlockAfterSpecifiedTime() throws InterruptedException {
		sut = new Unlocker(coRepository);
		when(coRepository.exists(key)).thenReturn(true);
		when(coRepository.unlock(key)).thenReturn(true);
		
		sut.unlockAfter(key, 100);
		
		Thread.sleep(150);
		verify(coRepository).unlock(key);
	}

	@Test
	public void shouldRetryWhenUnlockIsFailed() throws InterruptedException {
		sut = new Unlocker(coRepository);
		when(coRepository.exists(key)).thenReturn(true);
		when(coRepository.unlock(key)).thenReturn(false);
		
		sut.unlockAfter(key, 10);
		Thread.sleep(100);
		
		verify(coRepository, atLeast(3)).unlock(key);
	}
}
