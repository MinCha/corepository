package com.github.corepo.client.performance;

import org.junit.Ignore;
import org.junit.Test;

import com.github.corepo.client.CoRepository;
import com.github.corepo.client.CoRepositoryClient;
import com.github.corepo.client.Item;
import com.github.corepo.client.LocalMemoryCoRepository;
import com.github.corepo.client.support.FakeOriginalRepository;
import com.github.corepo.client.support.FakeVisitationDAO;

@Ignore
public class CoRepositoryInsertAndSelect {
	@Test
	public void firstTouch_WithLock() {
		CoRepository coRepository = new LocalMemoryCoRepository();
		FakeOriginalRepository fakeOriginalRepository = new FakeOriginalRepository(new FakeVisitationDAO());
		CoRepositoryClient client = new CoRepositoryClient(coRepository, fakeOriginalRepository);
		
		long current = System.currentTimeMillis();

		for (int i = 0; i < 100000; i++) {
			client.update(new Item("K" + i, i));
		}
		for (int i = 0; i < 100000; i++) {
			client.selectAsInt("K" + i);
		}
		
		System.out.println("First Touch CorepositoryClient Time : " + (System.currentTimeMillis() - current));
		
		current = System.currentTimeMillis();

		for (int i = 0; i < 100000; i++) {
			coRepository.update(new Item("K2" + i, i));
		}
		for (int i = 0; i < 100000; i++) {
			coRepository.selectAsInt("K2" + i);
		}
		
		System.out.println("First Touch Corepository Time : " + (System.currentTimeMillis() - current));
	}

	@Test
	public void secondTouch_WithNoLock() {
		CoRepository coRepository = new LocalMemoryCoRepository();
		FakeOriginalRepository fakeOriginalRepository = new FakeOriginalRepository(new FakeVisitationDAO());
		CoRepositoryClient client = new CoRepositoryClient(coRepository, fakeOriginalRepository);
		
		long current = System.currentTimeMillis();

		for (int i = 0; i < 100000; i++) {
			client.update(new Item("K" + i, i));
		}
		for (int i = 0; i < 100000; i++) {
			client.selectAsInt("K" + i);
		}

		for (int i = 0; i < 100000; i++) {
			client.update(new Item("K" + i, i));
		}
		for (int i = 0; i < 100000; i++) {
			client.selectAsInt("K" + i);
		}

		System.out.println("Second Touch CorepositoryClient Time : " + (System.currentTimeMillis() - current));
		
		current = System.currentTimeMillis();

		for (int i = 0; i < 100000; i++) {
			coRepository.update(new Item("K2" + i, i));
		}
		for (int i = 0; i < 100000; i++) {
			coRepository.selectAsInt("K2" + i);
		}

		for (int i = 0; i < 100000; i++) {
			coRepository.update(new Item("K2" + i, i));
		}
		for (int i = 0; i < 100000; i++) {
			coRepository.selectAsInt("K2" + i);
		}

		System.out.println("Second Corepository Time : " + (System.currentTimeMillis() - current));
	}
}
