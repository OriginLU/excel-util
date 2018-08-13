package com.chl.excel.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author LCH
 * @since 2018-06-22
 */
public class Sequence {

    //初始时间截 (2017-01-01)
    private static final long INITIAL_TIME_STAMP = 1483200000000L;

    private static final long WORKER_ID_BITS = 5L;

    private static final long DATACENTER_ID_BITS = 5L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    private final long SEQUENCE_BITS = 12L;

    private final long WORKERID_OFFSET = SEQUENCE_BITS;

    private final long DATACENTERID_OFFSET = SEQUENCE_BITS + SEQUENCE_BITS;

    private final long TIMESTAMP_OFFSET = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private long workerId;

    private long datacenterId;

    private long sequence = 0L;

    private long lastTimestamp = -1L;

    /**
     * 构造函数
     *
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public Sequence(long workerId, long datacenterId) {

        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("WorkerID 不能大于 %d 或小于 0", MAX_WORKER_ID));
        }

        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("DataCenterID 不能大于 %d 或小于 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 获得下一个ID (用同步锁保证线程安全)
     *
     * @return SnowflakeId
     */
    public synchronized Long nextId() {

        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {                 //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
            throw new RuntimeException("当前时间小于上一次记录的时间戳！");
        }
        if (lastTimestamp == timestamp) {                //如果是同一时间生成的，则进行毫秒内序列
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {                          //sequence等于0说明毫秒内序列已经增长到最大值
                timestamp = tilNextMillis(lastTimestamp); //阻塞到下一个毫秒,获得新的时间戳
            }
        } else {                                            //时间戳改变，毫秒内序列重置
            sequence = 0L;
        }
        lastTimestamp = timestamp;//上次生成ID的时间截

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - INITIAL_TIME_STAMP) << TIMESTAMP_OFFSET)
                | (datacenterId << DATACENTERID_OFFSET)
                | (workerId << WORKERID_OFFSET)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {

        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public static void main(String[] args) {

        final Sequence idGenerator = new Sequence(1, 1);
        //线程池并行执行10000次ID生成
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10000; i++) {
            executorService.execute(
                    new Runnable() {

                        @Override
                        public void run() {
                            long id = idGenerator.nextId();
                            System.out.println(id);
                        }
                    });
        }
        executorService.shutdown();
    }
}
