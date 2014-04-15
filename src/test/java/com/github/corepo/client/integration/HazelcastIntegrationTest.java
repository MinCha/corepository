package com.github.corepo.client.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

import com.hazelcast.core.Hazelcast;

public class HazelcastIntegrationTest {
    @Test
    public void shouldRunAsExpected() throws InterruptedException {
	Map<String, String> map = Hazelcast.getMap("corepo");
	map.put("name", "min");

	assertThat(map.get("name"), is("min"));
    }
}
