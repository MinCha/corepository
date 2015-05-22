package com.github.corepo.client.integration;

import com.github.corepo.client.*;
import com.github.corepo.client.measurement.support.NameAge;
import com.github.corepo.client.measurement.support.VisitationDAO;
import com.github.corepo.client.measurement.support.VisitationOriginalRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * At least, CoRepository implementations should pass following test-cases.
 *
 * @author Min Cha
 */
public abstract class CoRepositoryAcceptanceTest {
    protected CoRepository sut;

    protected final OriginalRepository originalRepository = new VisitationOriginalRepository(
            new VisitationDAO());
    protected final ItemKey key = new ItemKey("key" + System.currentTimeMillis());
    protected final ItemKey keyForLockA = new ItemKey("keyA" + System.currentTimeMillis());
    protected final ItemKey keyForLockB = new ItemKey("keyB" + System.currentTimeMillis());
    protected final ItemKey noKey = new ItemKey("noKey" + System.currentTimeMillis());
    protected final ItemKey intKey = new ItemKey("int" + System.currentTimeMillis());
    protected final ItemKey stringKey = new ItemKey("string" + System.currentTimeMillis());
    private int lockedCount = 0;

    protected abstract CoRepository getCoRepository() throws Exception;

    @Test
    public void canSelectIntValue() {
        final int value = 3;
        sut.insert(new Item(key, value));

        Item result = sut.selectAsInt(key);

        assertThat(result.getItemKey(), is(key));
        assertThat(result.getValueAsInt(), is(value));
    }

    @Test
    public void shouldReturnEmptyItem_WhenNoItemAsInteger() {
        Item result = sut.selectAsInt(noKey);

        assertThat(result.isNotFound(), is(true));
    }

    @Test
    public void shouldReturnEmptyItem_WhenNoItemAsString() {
        Item result = sut.selectAsObject(noKey);

        assertThat(result.isNotFound(), is(true));
    }

    @Test
    public void canSelectObject() {
        NameAge nameAge = new NameAge("min", 33);
        sut.insert(new Item(key, nameAge));

        Item item = sut.selectAsObject(key);

        NameAge result = (NameAge) item.getValue();
        assertThat(result, is(nameAge));
    }

    @Test
    public void canSelectStringValue() {
        final String value = "311";
        sut.insert(new Item(key, value));

        Item result = sut.selectAsObject(key);

        assertThat(result.getItemKey(), is(key));
        assertThat(result.getValueAsString(), is(value));
    }

    @Test
    public void canUpdateValue() {
        final int value = 3;
        final int newValue = 5;
        sut.insert(new Item(key, value));

        sut.update(new Item(key, newValue));

        Item result = sut.selectAsInt(key);
        assertThat(result.getItemKey(), is(key));
        assertThat(result.getValueAsInt(), is(newValue));
    }

    @Test
    public void canIncreaseValue() {
        final int value = 3;
        sut.insert(new Item(key, value));

        sut.increase(key);

        Item result = sut.selectAsInt(key);
        assertThat(result.getItemKey(), is(key));
        assertThat(result.getValueAsInt(), is(value + 1));
    }

    @Test
    public void keyShouldBePutAutomatically_WhenIncreasingNonExistingKey() {
        sut.increase(noKey);

        Item result = sut.selectAsInt(noKey);
        assertThat(result.getItemKey(), is(noKey));
        assertThat(result.getValueAsInt(), is(1));
    }

    @Test
    public void canDecreaseValue() {
        final int value = 3;
        sut.insert(new Item(key, value));

        sut.decrease(key);

        Item result = sut.selectAsInt(key);
        assertThat(result.getItemKey(), is(key));
        assertThat(result.getValueAsInt(), is(value - 1));
    }

    @Test
    public void keyShouldBePutAutomatically_WhenDecreasingNonExistingKey() {
        sut.decrease(noKey);

        Item result = sut.selectAsInt(noKey);
        assertThat(result.getItemKey(), is(noKey));
        assertThat(result.getValueAsInt(), is(-1));
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
    public void canDeleteKey() {
        sut.insert(new Item(key, 1));
        sut.increase(key);

        sut.delete(key);

        assertThat(sut.exists(key), is(false));
    }

    @Test
    public void canKnowWhetherKeyTypeIsIntegerOrNot() throws Exception {
        sut.insert(new Item(intKey, 1));
        sut.insert(new Item(stringKey, "value"));

        assertThat(sut.isInt(intKey), is(true));
        assertThat(sut.isInt(stringKey), is(false));
    }

    @MultiThreadTest
    @Test
    public void multipleClientsShouldShareOneCoRepositoryWithoutConflict()
            throws InterruptedException {
        final int clientCount = 50;
        final int callCount = 300;
        ExecutorService executors = Executors.newFixedThreadPool(clientCount);

        for (int i = 0; i < clientCount; i++) {
            final CoRepositoryClient client = new CoRepositoryClient(sut,
                    originalRepository);
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
        Item result = sut.selectAsInt(key);
        assertThat(result.isNotFound(), is(false));
        assertThat(result.getValueAsInt(), is(clientCount * callCount));
    }

    @MultiThreadTest
    @Test
    public void multipleClientsCanIncreaseOrDecreaseOnSameKeyWithoutConflict()
            throws Exception {
        increaseAndDecreaseByMultiThreadsOn(key);
    }

    @MultiThreadTest
    @Test
    public void multipleClientsCanIncreaseOrDecreaseOnNonExistingSameKeyWithoutConflict()
            throws Exception {
        increaseAndDecreaseByMultiThreadsOn(noKey);
    }

    private void increaseAndDecreaseByMultiThreadsOn(final ItemKey passedKey)
            throws InterruptedException {
        final int clientCount = 150;
        final int callCount = 150;
        ExecutorService executors = Executors.newFixedThreadPool(clientCount);

        for (int i = 0; i < clientCount / 2; i++) {
            final CoRepositoryClient client = new CoRepositoryClient(sut,
                    originalRepository);
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

        Item result = sut.selectAsInt(key);
        assertThat(result.getValueAsInt(), is(0));
    }

    @Test
    public void onlyOneClientShouldAcquireLock() throws Exception {
        for (int count = 0; count < 1; count++) {
            final int clientCount = 100;
            final int callCount = 100;
            final ItemKey currentKey = new ItemKey(key.getId() + System.currentTimeMillis());
            ExecutorService executors = Executors
                    .newFixedThreadPool(clientCount);

            for (int i = 0; i < clientCount; i++) {
                executors.submit(new Runnable() {
                    public void run() {
                        for (int i = 0; i < callCount; i++) {
                            if (sut.lock(currentKey)) {
                                lockedCount++;
                            }
                        }
                    }
                });
            }

            executors.shutdown();
            executors.awaitTermination(60, TimeUnit.SECONDS);

            assertThat(lockedCount, is(1));
            sut.unlock(currentKey);
            lockedCount = 0;
        }
    }

    @Before
    public void assignCoRepository() throws Exception {
        this.sut = getCoRepository();
    }

    @After
    public void clear() {
        sut.delete(key);
        sut.delete(noKey);
        sut.delete(stringKey);
        sut.delete(intKey);
        sut.delete(key.convertToLockedKey());
        sut.delete(keyForLockA.convertToLockedKey());
        sut.delete(keyForLockB.convertToLockedKey());
    }
}
