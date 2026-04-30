package com.zippyboot.kit.threadpool;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;

public class GlobalThreadFactory implements ThreadFactory {

    private static final String DEFAULT_PREFIX = "global-pool";
    private final AtomicInteger threadIndex = new AtomicInteger(1);
    private final String threadNamePrefix;

    public GlobalThreadFactory() {
        this(DEFAULT_PREFIX);
    }

    public GlobalThreadFactory(String threadNamePrefix) {
        this.threadNamePrefix = (threadNamePrefix == null || threadNamePrefix.trim().isEmpty())
                ? DEFAULT_PREFIX
                : threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(threadNamePrefix + "-" + threadIndex.getAndIncrement());
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (Thread.NORM_PRIORITY != thread.getPriority()) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
