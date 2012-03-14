package com.github.writeback.client;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.writeback.client.support.FakeOriginalRepository;
import com.github.writeback.client.support.FakeVisitationDAO;

/**
 * At least, CoRepository implementations should pass following test-cases.
 * 
 * @author Min Cha
 * 
 */
public abstract class CoRepositoryAcceptanceTest {
	protected CoRepository sut;
	protected final List<CoRepositoryClient> clients = new ArrayList<CoRepositoryClient>();

	protected final OriginalRepository originalRepository = new FakeOriginalRepository(
			new FakeVisitationDAO());
	protected final String key = "key";
	protected final String keyForLockA = "keyA";
	protected final String keyForLockB = "keyB";
	protected final String noKey = "noKey";

	protected abstract CoRepository getCoRepository() throws Exception;

	@Test
	public void canSelectIntValue() {
		final int value = 3;
		sut.insert(new Item(key, value));

		Item result = sut.selectAsInt(key);

		assertThat(result.getKey(), is(key));
		assertThat(result.getValueAsInt(), is(value));
	}

	@Test
	public void canSelectStringValueWithLastUpdatedTime() {
		final String value = "311";
		final String modifiedValue = "312";
		sut.insert(new Item(key, value));
		sut.update(new Item(key, modifiedValue));

		Item result = sut.selectAsString(key);

		assertThat(result.isUpdatedAfterPulling(), is(true));
		assertThat(result.getLastUpdatedTime(), is(greaterThan(0L)));
	}

	@Test
	public void canSelectIntValueWithLastUpdatedTime() {
		final int value = 10;
		final int modifiedValue = 12;
		final long updatedTime = System.currentTimeMillis(); 
		sut.insert(new Item(key, value));
		sut.update(new Item(key, modifiedValue, updatedTime));

		Item result = sut.selectAsInt(key);

		assertThat(result.isUpdatedAfterPulling(), is(true));
		assertThat(result.getLastUpdatedTime(), is(greaterThan(0L)));
	}

	@Test
	public void canSelectItemWithLastUpdatedTime_WhenNoUpdate() {
		final String value = "311";
		sut.insert(new Item(key, value));

		Item result = sut.selectAsString(key);

		assertThat(result.isUpdatedAfterPulling(), is(false));
	}

	@Test
	public void shouldReturnEmptyItem_WhenNoItemAsInteger() {
		Item result = sut.selectAsInt("noKey");

		assertThat(result.isNotFound(), is(true));
	}

	@Test
	public void shouldReturnEmptyItem_WhenNoItemAsString() {
		Item result = sut.selectAsString("noKey");

		assertThat(result.isNotFound(), is(true));
	}

	@Test
	public void canSelectStringValue() {
		final String value = "311";
		sut.insert(new Item(key, value));

		Item result = sut.selectAsString(key);

		assertThat(result.getKey(), is(key));
		assertThat(result.getValueAsString(), is(value));
	}

	@Test
	public void canUpdateValue() {
		final int value = 3;
		final int newValue = 5;
		final long updatedTime = System.currentTimeMillis();
		sut.insert(new Item(key, value));

		sut.update(new Item(key, newValue, updatedTime));

		Item result = sut.selectAsInt(key);
		assertThat(result.getKey(), is(key));
		assertThat(result.getValueAsInt(), is(newValue));
	}

	@Test
	public void canIncreaseValue() {
		final int value = 3;
		final long updatedTime = System.currentTimeMillis();
		sut.insert(new Item(key, value, updatedTime));

		sut.increase(key);

		Item result = sut.selectAsInt(key);
		assertThat(result.getKey(), is(key));
		assertThat(result.getValueAsInt(), is(value + 1));
	}

	@Test
	public void canDecreaseValue() {
		final int value = 3;
		sut.insert(new Item(key, value));

		sut.decrease(key);

		Item result = sut.selectAsInt(key);
		assertThat(result.getKey(), is(key));
		assertThat(result.getValueAsInt(), is(value - 1));
	}

	@Test
	public void canKnowWhetherThereIsKey() {
		sut.insert(new Item(key, new String("Sample")));

		assertThat(sut.exists(key), is(true));
		assertThat(sut.exists(noKey), is(false));
	}

	@Test
	public void canLockKey() {
		assertThat(sut.lock(keyForLockA), is(true));
		assertThat(sut.lock(keyForLockA), is(false));
		assertThat(sut.lock(keyForLockB), is(true));
	}

	@Test
	public void canUnockKey() {
		assertThat(sut.lock(keyForLockA), is(true));
		assertThat(sut.lock(keyForLockA), is(false));
		assertThat(sut.unlock(keyForLockA), is(true));
		assertThat(sut.lock(keyForLockA), is(true));
	}

	@Test
	public void mulpipleClientsShouldShareOneCoRepositoryWithoutConflict()
			throws InterruptedException {
		final int clientCount = 50;
		final int callCount = 300;
		ExecutorService executors = Executors.newFixedThreadPool(clientCount);

		for (int i = 0; i < clientCount; i++) {
			final CoRepositoryClient client = new CoRepositoryClient(sut,
					originalRepository);
			clients.add(client);
			executors.submit(new Runnable() {
				public void run() {
					for (int i = 0; i < callCount; i++) {
						client.increase(key);
					}
				}
			});
		}
		executors.shutdown();
		executors.awaitTermination(60, TimeUnit.SECONDS);

		Thread.sleep(1000 * 5);
		Item result = clients.get(0).selectAsInt(key);
		assertThat(result.getValueAsInt(), is(clientCount * callCount));
	}

	@Test
	public void multipleClientsCanIncreaseOrDecreaseOnSameKeyWithoutConflict()
			throws InterruptedException {
		final int clientCount = 50;
		final int callCount = 300;
		ExecutorService executors = Executors.newFixedThreadPool(clientCount);

		for (int i = 0; i < clientCount / 2; i++) {
			final CoRepositoryClient client = new CoRepositoryClient(sut,
					originalRepository);
			clients.add(client);
			executors.submit(new Runnable() {
				public void run() {
					for (int i = 0; i < callCount; i++) {
						client.increase(key);
					}
				}
			});
		}
		for (int i = clientCount / 2; i < clientCount; i++) {
			final CoRepositoryClient client = new CoRepositoryClient(sut,
					originalRepository);
			clients.add(client);
			executors.submit(new Runnable() {
				public void run() {
					for (int i = 0; i < callCount; i++) {
						client.decrease(key);
					}
				}
			});
		}

		executors.shutdown();
		executors.awaitTermination(60, TimeUnit.SECONDS);

		Item result = clients.get(0).selectAsInt(key);
		assertThat(result.getValueAsInt(), is(0));
	}

	@Before
	public void assignCoRepository() throws Exception {
		this.sut = getCoRepository();
	}

	@After
	public void clear() {
		sut.delete(key);
		sut.delete(noKey);
		sut.delete(TTCoRepository.LOCK_KEY_PREFIX + keyForLockA);
		sut.delete(TTCoRepository.LOCK_KEY_PREFIX + keyForLockB);
		sut.delete(Item.META_PREFIX + key);
		sut.delete(Item.META_PREFIX + noKey);
		sut.delete(Item.META_PREFIX + TTCoRepository.LOCK_KEY_PREFIX + keyForLockA);
		sut.delete(Item.META_PREFIX + TTCoRepository.LOCK_KEY_PREFIX + keyForLockB);

		assertThat(sut.exists(key), is(false));
		assertThat(sut.exists(noKey), is(false));
	}
}
