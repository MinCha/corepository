package com.github.corepo.client;

import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    public Item selectAsObject(ItemKey key) {
        Jedis jedis = jedisPool.getResource();
        try {
            byte[] value = jedis.get(key.getKey().getBytes());
            if (value == null) {
                return Item.withNoValue(key);
            }
            return new Item(key, deserialize(value));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    public Item selectAsInt(ItemKey key) {
        Jedis jedis = jedisPool.getResource();
        try {
            String value = jedis.get(key.getKey());
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
                jedis.set(item.getItemKeyAsString(), String.valueOf(item.getValueAsInt()));
            } else {
                jedis.set(item.getItemKeyAsString().getBytes(), serialize(item.getValue()));
            }
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    public int increase(ItemKey key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return Ints.checkedCast(jedis.incr(key.getKey()));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    public int decrease(ItemKey key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return Ints.checkedCast(jedis.decr(key.getKey()));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    public boolean exists(ItemKey key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.exists(key.getKey());
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    public boolean delete(ItemKey key) {
        Jedis jedis = jedisPool.getResource();
        try {
            return jedis.del(key.getKey()) == 1;
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    public boolean lock(ItemKey key) {
        final int winner = 1;
        int result = increase(key.convertToLockedKey());
        if (winner == result) {
            LOG.info(key + " winner");
        }
        return winner == result;
    }

    public boolean unlock(ItemKey key) {
        return delete(key.convertToLockedKey());
    }

    public boolean isInt(ItemKey key) {
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
