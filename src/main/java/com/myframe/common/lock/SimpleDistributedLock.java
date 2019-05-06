package com.myframe.common.lock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author lch
 * @since 2018-11-03
 */
public class SimpleDistributedLock {


    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final ThreadLocal<String> LOCK_VALUE = new ThreadLocal<>();

    private static final ThreadLocal<String> LOCK_KEY = new ThreadLocal<>();

    private static final Long RELEASE_SUCCESS = 1L;

    private static final String LOCK = "LOCK:";

    private static final String LOCK_SUCCESS = "OK";

    private static final String SET_IF_NOT_EXIST = "NX";

    private static final String SET_WITH_EXPIRE_TIME = "PX";


    private final JedisPool jedisPool;

    public SimpleDistributedLock(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }


    public boolean tryLock(String key){
        return tryLock(key, TimeUnit.SECONDS.toMillis(5),TimeUnit.SECONDS.toMillis(60));
    }

    /**
     * 加锁
     */
    public boolean tryLock(String key, long acquireTimeout, long timeout) {

        try (Jedis conn = jedisPool.getResource())
        {
            conn.select(0);
            String lockKey = LOCK + key;
            String identifier = UUID.randomUUID().toString().replace("-","");
            long end = System.currentTimeMillis() + acquireTimeout; // 获取锁的超时时间，超过这个时间则放弃获取锁
            while (System.currentTimeMillis() < end)
            {
                String ok = conn.set(lockKey, identifier, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, timeout);
                if (LOCK_SUCCESS.equals(ok))
                {
                    LOCK_KEY.set(key);
                    LOCK_VALUE.set(identifier);
                    return true;
                }
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                    log.error("线程中断 : {}", e);
                }
            }
        }
        return false;
    }

    /**
     * 释放锁
     */
    public boolean release() {
        
        Jedis conn = null;
        try {
            String lockKey = LOCK_KEY.get();
            String lockId = LOCK_VALUE.get();
            if (StringUtils.isNotBlank(lockId))
            {
                conn = jedisPool.getResource();
                conn.select(0);
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Object result = conn.eval(script, Collections.singletonList(lockKey), Collections.singletonList(lockId));
                if (RELEASE_SUCCESS.equals(result))
                {
                    LOCK_VALUE.remove();
                    LOCK_KEY.remove();
                    return true;
                }
                return false;

            }
        }
        catch (JedisException e)
        {
            log.error("redis exception ", e);
        }
        finally
        {
            if (conn != null)
            {
                conn.close();
            }
        }
        return false;
    }

}
