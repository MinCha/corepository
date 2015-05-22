package com.github.corepo.client;

import com.google.common.cache.RemovalNotification;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class KeyRemovalListenerTest {
    private KeyRemovalListener sut;

    @Mock
    private Writeback writeback;
    private RemovalNotification<String, UpdateTime> notification;
    private final String key = "key";

    @Test
    public void shouldWritebackItem() {
        sut = new KeyRemovalListener(writeback) {
            @Override
            String getKey(RemovalNotification<String, UpdateTime> notification) {
                return key;
            }
        };

        sut.onRemoval(notification);

        verify(writeback).execute(key);
    }
}
