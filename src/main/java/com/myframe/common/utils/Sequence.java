package com.myframe.common.utils;

/**
 * @author LCH
 * @since 2018-06-22
 */
public class Sequence {

    private static final long INITIAL_TIME_STAMP = 1557720000000L;

    private static final long WORKER_ID_BITS = 5L;

    private static final long DATA_CENTER_ID_BITS = 5L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    private static final long SEQUENCE_BITS = 12L;

    private static final long WORKER_ID_OFFSET = SEQUENCE_BITS;

    private static final long DATA_CENTER_ID_OFFSET = SEQUENCE_BITS + SEQUENCE_BITS;

    private static final long TIMESTAMP_OFFSET = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private long workerId;

    private long dataCenterId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    /**
     * 构造函数
     */
    public Sequence(long workerId, long dataCenterId) {

        if (workerId > MAX_WORKER_ID || workerId < 0)
        {
            throw new IllegalArgumentException(String.format("WorkerID 不能大于 %d 或小于 0", MAX_WORKER_ID));
        }

        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0)
        {
            throw new IllegalArgumentException(String.format("DataCenterID 不能大于 %d 或小于 0", MAX_DATA_CENTER_ID));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 获得下一个ID (用同步锁保证线程安全)
     */
    public synchronized Long nextId() {

        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp)
        {
            throw new RuntimeException("当前时间小于上一次记录的时间戳！");
        }
        if (lastTimestamp == timestamp)   //如果是同一时间生成的，则进行毫秒内序列
        {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0)
            {
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        else
        {
            sequence = 0L;
        }
        lastTimestamp = timestamp;

        return ((timestamp - INITIAL_TIME_STAMP) << TIMESTAMP_OFFSET)
                | (dataCenterId << DATA_CENTER_ID_OFFSET)
                | (workerId << WORKER_ID_OFFSET)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {

        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp)
        {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }


}
