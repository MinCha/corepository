package com.github.corepo.client;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ItemKeyTest {
    private ItemKey sut;

    @Test
    public void canCreateKey() {
        final String nameSpace = "following";
        final String id = "1";

        sut = new ItemKey(nameSpace, id);

        assertThat(sut.getNameSpace(), is(nameSpace));
        assertThat(sut.getId(), is(id));
    }

    @Test
    public void canCreateKeyWIthoutNameSpace() {
        final String id = "1";

        sut = new ItemKey(id);

        assertThat(sut.getId(), is(id));
    }

    @Test
    public void shouldHaveDefaultNameSpaceWhenCreatedWithoutNameSpace() {
        final String id = "1";

        sut = new ItemKey(id);

        assertThat(sut.getNameSpace(), is(ItemKey.DEFAULT_NAMESPACE));
    }

    @Test
    public void canComposeNameSpaceAndKey() {
        final String nameSpace = "following";
        final String id = "1";

        sut = new ItemKey(nameSpace, id);

        assertThat(sut.getKey(), is(nameSpace + ItemKey.KEY_DELIM + id));
    }

    @Test
    public void canGenerateLockKey() {
        final String nameSpace = "following";
        final String id = "1";

        sut = new ItemKey(nameSpace, id).convertToLockedKey();

        assertThat(sut.getKey(), is(ItemKey.LOCK_PREFIX + nameSpace + ItemKey.KEY_DELIM + id));
    }
}
