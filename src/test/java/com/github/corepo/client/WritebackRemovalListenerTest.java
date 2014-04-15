package com.github.corepo.client;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.cache.RemovalNotification;

@RunWith(MockitoJUnitRunner.class)
public class WritebackRemovalListenerTest {
    private WritebackRemovalListener sut;

    @Mock
    private OriginalRepository originalRepository;
    @Mock
    private CoRepository coRepository;
    private RemovalNotification<String, UpdateTime> notification;
    private final String key = "key";
    private final Item intItem = new Item(key, 11);
    private final Item stringItem = new Item(key, "some value");

    @Test
    public void shouldWritebackCurrentItem_WhenItemIsStringType() {
	sut = new WritebackRemovalListener(coRepository, originalRepository) {
	    @Override
	    String getKey(RemovalNotification<String, UpdateTime> notification) {
		return key;
	    }
	};
	when(coRepository.exists(key)).thenReturn(true);
	when(coRepository.isInt(key)).thenReturn(false);
	when(coRepository.selectAsObject(key)).thenReturn(stringItem);

	sut.onRemoval(notification);

	verify(originalRepository).writeback(Arrays.asList(stringItem));
    }

    @Test
    public void shouldWritebackCurrentItem_WhenItemIsIntType() {
	sut = new WritebackRemovalListener(coRepository, originalRepository) {
	    @Override
	    String getKey(RemovalNotification<String, UpdateTime> notification) {
		return key;
	    }
	};
	when(coRepository.exists(key)).thenReturn(true);
	when(coRepository.isInt(key)).thenReturn(true);
	when(coRepository.selectAsInt(key)).thenReturn(intItem);

	sut.onRemoval(notification);

	verify(originalRepository).writeback(Arrays.asList(intItem));
    }
}
