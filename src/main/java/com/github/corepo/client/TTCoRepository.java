package com.github.corepo.client;

import tokyotyrant.MRDB;
import tokyotyrant.networking.NodeAddress;
import tokyotyrant.transcoder.IntegerTranscoder;
import tokyotyrant.transcoder.StringTranscoder;

public class TTCoRepository implements CoRepository {
    public static final String LOCK_KEY_PREFIX = "_CO_REPOSITORY_LOCK_FOR_";
    private MRDB tt;
    private boolean connected = true;

    private final IntegerTranscoder integerTranscoder = new IntegerTranscoder();

    public TTCoRepository(String ip, int port) throws Exception {
        tt = new MRDB();
        tt.setGlobalTimeout(2000);
        tt.open(NodeAddress.addresses("tcp://" + ip + ":" + port));
    }

    public TTCoRepository(MRDB tt) {
        this.tt = tt;
    }

    public void update(Item item) {
        insert(item);
    }

    public void insert(Item item) {
        if (item.isInteger()) {
            tt.await(tt.put(item.getItemKeyAsString(), item.getValueAsInt(),
                    integerTranscoder));
        } else {
            tt.await(tt.put(item.getItemKeyAsString(), item.getValue()));
        }
    }

    public int increase(ItemKey key) {
        int result = tt.await(tt.addint(key, 1));
        return result;
    }

    public int decrease(ItemKey key) {
        int result = tt.await(tt.addint(key, -1));
        return result;
    }

    public boolean exists(ItemKey key) {
        return tt.await(tt.get(key, new StringTranscoder())) != null;
    }

    public boolean lock(ItemKey key) {
        final int winner = 1;
        int result = increase(key.convertToLockedKey());
        return winner == result;
    }

    public boolean unlock(ItemKey key) {
        return delete(key.convertToLockedKey());
    }

    public boolean delete(ItemKey key) {
        return tt.await(tt.out(key));
    }

    public Item selectAsObject(ItemKey key) {
        Object value = tt.await(tt.get(key));
        if (value == null) {
            return Item.withNoValue(key);
        }
        return new Item(key, value);
    }

    public Item selectAsInt(ItemKey key) {
        Object value = tt.await(tt.get(key, integerTranscoder));
        if (value == null) {
            return Item.withNoValue(key);
        }
        return new Item(key, (Integer) value);
    }

    public boolean isInt(ItemKey key) {
        try {
            selectAsInt(key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void close() {
        tt.close();
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }
}
