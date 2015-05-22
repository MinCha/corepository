package com.github.corepo.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CoRepositoryClientTest {
    private CoRepositoryClient sut;

    private final ItemKey key = new ItemKey("key");
    private final ItemKey noKey = new ItemKey("noKey");
    private final Item integerItem = new Item(key, 3);
    private final Item stringItem = new Item(key, "3");
    @Mock
    private CoRepository coRepository;
    @Mock
    private OriginalRepository originalRepository;
    @Mock
    private LRUKeyUpdateTime keyUpdateTime;

    @Before
    public void beforeEach() {
        when(coRepository.lock(key)).thenReturn(true);
    }

    @Test
    public void canSelectStringValue() {
        when(originalRepository.read(key)).thenReturn(stringItem);
        when(coRepository.selectAsObject(key)).thenReturn(stringItem);
        sut = new CoRepositoryClient(coRepository, originalRepository);

        Item result = sut.selectAsObject(key);

        assertThat(result, is(stringItem));
    }

    @Test
    public void canSelectIntegerValue() {
        when(originalRepository.read(key)).thenReturn(integerItem);
        when(coRepository.selectAsInt(key)).thenReturn(integerItem);
        sut = new CoRepositoryClient(coRepository, originalRepository);

        Item result = sut.selectAsInt(key);

        assertThat(result, is(integerItem));
    }

    @Test
    public void canUpdateValue() {
        when(originalRepository.read(key)).thenReturn(integerItem);
        sut = new CoRepositoryClient(coRepository, originalRepository,
                keyUpdateTime, 1);

        sut.update(stringItem);

        verify(coRepository).update(stringItem);
        verify(keyUpdateTime, times(2)).notifyUpdated(Mockito.eq(key),
                Mockito.anyLong());
    }

    @Test
    public void canIncreaseValue() {
        when(originalRepository.read(key)).thenReturn(integerItem);
        sut = new CoRepositoryClient(coRepository, originalRepository,
                keyUpdateTime, 1);

        sut.increase(key);

        verify(coRepository).increase(key);
        verify(keyUpdateTime, times(2)).notifyUpdated(Mockito.eq(key),
                Mockito.anyLong());
    }

    @Test
    public void canDecreaseValue() {
        when(originalRepository.read(key)).thenReturn(integerItem);
        sut = new CoRepositoryClient(coRepository, originalRepository,
                keyUpdateTime, 1);

        sut.decrease(key);

        verify(coRepository).decrease(key);
        verify(keyUpdateTime, times(2)).notifyUpdated(Mockito.eq(key),
                Mockito.anyLong());
    }

    @Test
    public void shouldThrowException_WhenOperatingWithNoKey() {
        when(coRepository.lock(noKey)).thenReturn(true);
        when(originalRepository.read(noKey))
                .thenReturn(Item.withNoValue(noKey));
        sut = new CoRepositoryClient(coRepository, originalRepository);

        try {
            when(coRepository.selectAsInt(noKey)).thenReturn(
                    Item.withNoValue(noKey));
            sut.selectAsInt(noKey);
            fail();
        } catch (NonExistentKeyException e) {
        }

        try {
            sut.update(new Item(noKey, "anyValue"));
            fail();
        } catch (NonExistentKeyException e) {
        }

        try {
            sut.increase(noKey);
            fail();
        } catch (NonExistentKeyException e) {
        }

        try {
            sut.decrease(noKey);
            fail();
        } catch (NonExistentKeyException e) {
        }
    }
}
