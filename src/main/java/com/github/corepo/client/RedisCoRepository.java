package com.github.corepo.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.google.common.primitives.Ints;

public class RedisCoRepository implements CoRepository {
	private static final Logger LOG = LoggerFactory
			.getLogger(RedisCoRepository.class);
	private static final String LOCK_KEY_PREFIX = "_CO_REPOSITORY_LOCK_FOR_";
	private JedisPool jedisPool;
	private boolean connected = true;

	public RedisCoRepository(String host, int port) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(100);
		this.jedisPool = new JedisPool(config, host, port);
	}

	public RedisCoRepository(JedisPool jedis) {
		this.jedisPool = jedis;
	}

	public Item selectAsObject(String key) {
		Jedis jedis = jedisPool.getResource();
		try {
			byte[] value = jedis.get(key.getBytes());
			if (value == null) {
				return Item.withNoValue(key);
			}
			return new Item(key, deserialize(value));
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public Item selectAsInt(String key) {
		Jedis jedis = jedisPool.getResource();
		try {
			String value = jedis.get(key);
			if (value == null) {
				return Item.withNoValue(key);
			}
			return new Item(key, Integer.parseInt(value));
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public void update(Item item) {
		insert(item);
	}

	public void insert(Item item) {
		Jedis jedis = jedisPool.getResource();
		try {
			if (item.isInteger()) {
				jedis.set(item.getKey(), String.valueOf(item.getValueAsInt()));
			} else {
				jedis.set(item.getKey().getBytes(), serialize(item.getValue()));
			}
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public int increase(String key) {
		Jedis jedis = jedisPool.getResource();
		try {
			return Ints.checkedCast(jedis.incr(key));
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public int decrease(String key) {
		Jedis jedis = jedisPool.getResource();
		try {
			return Ints.checkedCast(jedis.decr(key));
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public boolean exists(String key) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.exists(key);
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public boolean delete(String key) {
		Jedis jedis = jedisPool.getResource();
		try {
			return jedis.del(key) == 1;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public boolean lock(String key) {
		final int winner = 1;
		int result = increase(LOCK_KEY_PREFIX + key);
		if (winner == result) {
			LOG.info(key + " winner");
		}
		return winner == result;
	}

	public boolean unlock(String key) {
		return delete(LOCK_KEY_PREFIX + key);
	}

	public boolean isInt(String key) {
		try {
			selectAsInt(key);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private byte[] serialize(Object obj) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(b);
			o.writeObject(obj);
			return b.toByteArray();
		} catch (Exception e) {
			throw new SerializationException(e.toString(), e);
		}
	}

	private Object deserialize(byte[] bytes) {
		try {
			ByteArrayInputStream b = new ByteArrayInputStream(bytes);
			ObjectInputStream o = new ObjectInputStream(b);
			return o.readObject();
		} catch (Exception e) {
			throw new SerializationException(e.toString(), e);
		}
	}

	public void close() {
		connected = false;
	}

	public boolean isConnected() {
		return connected;
	}
}
