package com.github.corepo.client.integration;

import com.github.corepo.client.*;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LocalMemoryCoRepositoryIntegrationTest extends
        CoRepositoryAcceptanceTest {
    @Test
    public void canSaveAtLeastOneMillionItems() {
        final String keyPrefix = "domain key is ";
        final int oneMillion = 100 * 10000;
        for (int i = 0; i < oneMillion; i++) {
            sut.insert(new Item(new ItemKey(keyPrefix + i), i));
        }

        assertThat(sut.selectAsInt(new ItemKey(keyPrefix + "0")).getValueAsInt(), is(0));
        assertThat(sut.selectAsInt(new ItemKey(keyPrefix + oneMillion / 2)).getValueAsInt(),
                is(oneMillion / 2));
    }

    @Test(expected = NotNumericValueException.class)
    public void shouldThrowException_WhenIncreasingNotNumericValue() {
        final String stringValue = "high";
        sut.insert(new Item(key, stringValue));

        sut.increase(key);
    }

    @Test(expected = NotNumericValueException.class)
    public void shouldThrowException_WhenDecreasingNotNumericValue() {
        final String stringValue = "high";
        sut.insert(new Item(key, stringValue));

        sut.decrease(key);
    }

    @Override
    protected CoRepository getCoRepository() {
        return new LocalMemoryCoRepository();
    }
}
