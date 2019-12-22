package com.chuan.lock.redis;

import com.chuan.lock.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Distributed lock with Redis.
 *
 * @author hechuan
 */
@Service
public class DistributedLockWithRedis implements Lock {

    private static final String KEY = "LOCK_KEY";

    @Autowired
    private JedisConnectionFactory factory;

    private final ThreadLocal<String> local = new ThreadLocal<>();

    @Override
    public void lock() {
        if (tryLock()) { return; }

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        lock();
    }

    @Override
    public boolean tryLock() {
        Jedis jedis = (Jedis) factory.getConnection().getNativeConnection();
        // generate the lock value
        String uuid = UUID.randomUUID().toString();

        // try to get the lock by setting the lock "KEY: uuid" if it not exist and expiration time is 1 ms.
        // the expiration time need execute stress tests to measure the time of finishing a business transaction.
        String res = jedis.set(KEY, uuid, "NX", "PX", 1);

        // if get the lock successfully
        if ("OK".equals(res)) {
            // cache the lock value for unlocking
            local.set(uuid);
            return true;
        }

        return false;
    }


    /**
     * Unlock by executing the lua script to avoid the multi-threads unsafe problems. Since the redis execute the lua
     * script one by one, and one script can guarantee the atomicity.
     *
     * Of course we can unlock through the Jedis remove APIs, but we need be careful about the multi-threads unsafe
     * problems.
     */
    @Override
    public void unlock() {
        // get the unlock script
        String script = FileUtils.getScript("unlock.lua");
        // get the redis native connection
        Jedis jedis = (Jedis) factory.getConnection().getNativeConnection();
        // execute the script to unlock by remove the lock "KEY: uuid"
        jedis.eval(script, Collections.singletonList(KEY), Collections.singletonList(local.get()));
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }



    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }



    @Override
    public Condition newCondition() {
        return null;
    }
}
