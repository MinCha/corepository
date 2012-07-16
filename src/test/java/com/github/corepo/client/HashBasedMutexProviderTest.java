package com.github.corepo.client;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.corepo.client.HashBasedMutexProvider;

public class HashBasedMutexProviderTest {
	private HashBasedMutexProvider sut;
	
	@Test
	public void shouldProvideSeperatedLocksByKey() {
		long a = 1L;
		long b = 2L;
		long c = 3L;
		
		final int lockCount = 3; 
		sut = new HashBasedMutexProvider(lockCount);
		
		assertThat(sut.get(a), is(not(sut.get(b))));
		assertThat(sut.get(b), is(not(sut.get(c))));
		assertThat(sut.get(c), is(not(sut.get(a))));
		assertThat(sut.get(a), is(sut.get(a)));
	}

	@Test
	public void shouldProvideLocksBySpecialKey() {
		final int lockCount = 25; 
		String key = "polygenelubricants";
		sut = new HashBasedMutexProvider(lockCount);
		
		assertThat(sut.get(key), is(notNullValue()));
	}
}
