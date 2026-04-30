package com.zippyboot.kit.threadpool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author niewj
 * @date 2022/5/13 20:40
 * @Description:
 */
@Slf4j
@Configuration
public class GlobalThreadPool {
    private static final int coreSizeCpu = Math.max(2, Runtime.getRuntime().availableProcessors());
    private static final int keepAliveTime = 60;

    private static final ConcurrentHashMap<Long, Future<?>> taskMap = new ConcurrentHashMap<>();

    public static final ExecutorService EXECUTOR =  new ThreadPoolExecutor(
            coreSizeCpu, // 核心线程数
            coreSizeCpu * 2, // 最大线程数
            keepAliveTime,      // 线程最大空闲时间
            TimeUnit.SECONDS,   // 时间单位
            new LinkedBlockingQueue<>(2000),    // 任务队列
            new GlobalThreadFactory("global-exec"),
            new ThreadPoolExecutor.CallerRunsPolicy()   // 线程池满，被拒绝执行线程的处理策略
    );

    public static Future<?> submit(Long id, Runnable task) {
        Future<?> future = EXECUTOR.submit(task);
        if (id != null) {
            taskMap.put(id, future);
        }
        return future;
    }

    public static Future<?> getTask(Long id) {
        return taskMap.get(id);
    }

    public static Future<?> setTask(Long id, Future<?> future) {
        return taskMap.put(id, future);
    }

    public static Future<?> removeTask(Long id) {
        return taskMap.remove(id);
    }

    public static boolean cancel(Long id) {
        Future<?> future = removeTask(id);
        if (future != null) {
            return future.cancel(true);
        }
        return false;
    }

    /**
     * @deprecated Use getTask instead.
     */
    @Deprecated
    public static Future<?> getThread(Long id) {
        return getTask(id);
    }

    /**
     * @deprecated Use setTask instead.
     */
    @Deprecated
    public static Future<?> setThread(Long id, Future<?> thread) {
        return setTask(id, thread);
    }

    /**
     * @deprecated Use removeTask instead.
     */
    @Deprecated
    public static Future<?> removeThread(Long id) {
        return removeTask(id);
    }

    /**
     * @deprecated Use cancel instead.
     */
    @Deprecated
    public static void stop(Long id) {
        cancel(id);
    }

}
