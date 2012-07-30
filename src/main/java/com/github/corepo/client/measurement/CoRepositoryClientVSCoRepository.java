package com.github.corepo.client.measurement;

import com.github.corepo.client.CoRepository;
import com.github.corepo.client.CoRepositoryClient;
import com.github.corepo.client.Item;
import com.github.corepo.client.LocalMemoryCoRepository;
import com.github.corepo.client.support.FakeOriginalRepository;
import com.github.corepo.client.support.FakeVisitationDAO;

public class CoRepositoryClientVSCoRepository {
	public static void main(String[] args) throws InterruptedException {
		highlightedTitle("FirstTouchWithLock");
		firstTouch_WithLock();
		highlightedTitle("SecondTouchWithNolock");
		secondTouch_WithNoLock();
	}

	public static void highlightedTitle(String title) throws InterruptedException {
		System.out.println("====================================================");
		System.out.println(title);
		System.out.println("====================================================");
	}

	public static void firstTouch_WithLock() throws InterruptedException {
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

	public static void secondTouch_WithNoLock() {
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

		System.out.println("Second Touch CorepositoryClient Time : " + (System.currentTimeMillis() - current));
		
		current = System.currentTimeMillis();

		for (int i = 0; i < 100000; i++) {
			coRepository.update(new Item("K2" + i, i));
		}
		for (int i = 0; i < 100000; i++) {
			coRepository.selectAsInt("K2" + i);
		}

		System.out.println("Second Corepository Time : " + (System.currentTimeMillis() - current));
	}
}
