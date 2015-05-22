package com.github.corepo.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <code>CoRepository</code> using local memory.
 * <p/>
 * If you use this class as CoRepository, you don`t need any remote
 * CoRepository.
 * <p/>
 * <b>Warning!</b> 1. In clustered environment such as web servers clustered by
 * L4 switch, this class does not ensure that select method returns the recent
 * value. 2. LocalMemoryCoRepository requires enough heap memory. Be careful of
 * out of memory.
 *
 * @author Min Cha
 */
public class LocalMemoryCoRepository implements CoRepository {
    private Map<ItemKey, Object> locks = new ConcurrentHashMap<ItemKey, Object>();
    private Map<ItemKey, Object> items = new ConcurrentHashMap<ItemKey, Object>();
    private HashBasedMutexProvider mutex = new HashBasedMutexProvider();
    private boolean connected = true;

    public void update(Item item) {
        synchronized (mutex.get(item.getItemKey())) {
            items.put(item.getItemKey(), item.getValue());
        }
    }

    public int increase(ItemKey key) {
        synchronized (mutex.get(key)) {
            int value = 0;
            try {
                Object original = items.get(key);
                if (original != null) {
                    value = (Integer) items.get(key);
                }
            } catch (ClassCastException e) {
                throw new NotNumericValueException();
            }
            value++;
            items.put(key, value);
            return value;
        }
    }

    public int decrease(ItemKey key) {
        synchronized (mutex.get(key)) {
            int value = 0;
            try {
                Object original = items.get(key);
                if (original != null) {
                    value = (Integer) items.get(key);
                }
            } catch (ClassCastException e) {
                throw new NotNumericValueException();
            }
            value--;
            items.put(key, value);
            return value;
        }
    }

    public void insert(Item item) {
        synchronized (mutex.get(item.getItemKeyAsString())) {
            items.put(item.getItemKey(), item.getValue());
        }
    }

    public boolean exists(ItemKey key) {
        return items.containsKey(key);
    }

    @SuppressWarnings("unused")
    private int convertIntFrom(String result) {
        int value;
        try {
            value = Integer.parseInt(result);
        } catch (Exception e) {
            throw new NotNumericValueException(result, e);
        }
        return value;
    }

    public boolean lock(ItemKey key) {
        synchronized (mutex.get(key)) {
            if (locks.containsKey(key)) {
                return false;
            } else {
                locks.put(key, new Object());
                return true;
            }
        }
    }

    public boolean unlock(ItemKey key) {
        if (locks.containsKey(key)) {
            locks.remove(key);
            return true;
        } else {
            return false;
        }
    }

    public boolean delete(ItemKey key) {
        return items.remove(key) != null;
    }

    public Item selectAsObject(ItemKey key) {
        if (items.containsKey(key) == false) {
            return Item.withNoValue(key);
        }

        return new Item(key, items.get(key));
    }

    public Item selectAsInt(ItemKey key) {
        if (items.containsKey(key) == false) {
            return Item.withNoValue(key);
        }

        int value = (Integer) items.get(key);
        return new Item(key, value);
    }

    public boolean isInt(ItemKey key) {
        return items.get(key) instanceof Integer;
    }

    public void close() {
        locks.clear();
        items.clear();
        connected = false;
    }

    public int size() {
        return items.size();
    }

    public boolean isConnected() {
        return connected;
    }
}
