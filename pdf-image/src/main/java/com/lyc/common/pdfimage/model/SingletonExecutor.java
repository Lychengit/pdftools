package com.lyc.common.pdfimage.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 单线程线程池
 */
public class SingletonExecutor extends ThreadPoolExecutor {

    private static final Logger log = LoggerFactory.getLogger(SingletonExecutor.class);

    private String name;

    public SingletonExecutor(final String poolName, int threadQueueSize) {
        super(1, 1, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(threadQueueSize),
                new ThreadFactoryBuilder()
                        .setNameFormat("-【"+ poolName+"】-thread-%d")
                        .setUncaughtExceptionHandler((t, e) -> {
                            e.printStackTrace();
                        }).build(),
                new AbortPolicy());
        this.name = poolName;
    }

    public String getName() {
        return name;
    }

    public int taskSize(){
        BlockingQueue<Runnable> queue = this.getQueue();
        if (queue == null){
            throw new RuntimeException("当前执行器队列为空");
        }
        return queue.size();
    }
}
