package com.github.corepo.client.integration;

import redis.clients.jedis.JedisPool;

import com.github.corepo.client.CoRepository;
import com.github.corepo.client.RedisCoRepository;

public class RedisCoRepositoryIntegrationTest extends
	CoRepositoryAcceptanceTest {
    private final String ip = "192.168.0.12"; // change this to your ip before
    // executing
    private final int port = 6379; // change this to your port before executing

    private JedisPool jedis;

    @Override
    protected CoRepository getCoRepository() throws Exception {
	jedis = new JedisPool(ip, port);
	return new RedisCoRepository(jedis);
    }
}
