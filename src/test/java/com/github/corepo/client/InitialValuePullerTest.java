package com.github.corepo.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InitialValuePullerTest {
    private InitialValuePuller sut;
    private final ItemKey key = new ItemKey("key");
    @Mock
    private CoRepository coRepository;
    @Mock
    private OriginalRepository originalRepository;
    @Mock
    private LRUKeyUpdateTime keyUpdateTime;

    @Test
    public void initialValueShouldBePulledToCoRepository() {
        Item initialValue = new Item(key, "anyValue");
        when(coRepository.lock(key)).thenReturn(true);
        when(originalRepository.read(key)).thenReturn(initialValue);
        sut = new InitialValuePuller(coRepository, originalRepository,
                keyUpdateTime);

        sut.ensurePulled(key);

        verify(coRepository).insert(initialValue);
    }

    @Test(expected = TimeoutException.class)
    public void shouldWaitPulling_UntilLimitedTime() {
        sut = new InitialValuePuller(coRepository, originalRepository,
                keyUpdateTime);

        sut.ensurePulled(key);
    }

    @Test(expected = NonExistentKeyException.class)
    public void shouldThrowException_WhenThereIsNoKeyOnOriginalRepository() {
        when(originalRepository.read(key)).thenReturn(Item.withNoValue(key));
        when(coRepository.lock(key)).thenReturn(true);
        sut = new InitialValuePuller(coRepository, originalRepository,
                keyUpdateTime);

        sut.ensurePulled(key);
    }

    @Test
    public void userKeyShouldBeCachedOnLocalMemory() {
        when(keyUpdateTime.exists(key)).thenReturn(true);
        sut = new InitialValuePuller(coRepository, originalRepository,
                keyUpdateTime);

        sut.ensurePulled(key);

        verifyZeroInteractions(coRepository);
    }
}
