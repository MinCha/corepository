package com.github.corepo.client.integration;

import com.hazelcast.core.Hazelcast;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HazelcastIntegrationTest {
    @Test
    public void shouldRunAsExpected() throws InterruptedException {
        Map<String, String> map = Hazelcast.getMap("corepo");
        map.put("name", "min");

        assertThat(map.get("name"), is("min"));
    }
}
