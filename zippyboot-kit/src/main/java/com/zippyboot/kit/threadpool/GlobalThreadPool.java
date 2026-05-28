package com.zippyboot.kit.threadpool;

import java.util.concurrent.*;

/**
 * 全局线程池工具。
 * <p>
 * 通过 {@link #submit(Long, Runnable)} 提交任务时可关联业务 ID，
 * 任务完成后自动从 taskMap 中移除，避免内存泄漏。
 */
public final class GlobalThreadPool {

    private static final int CORE_SIZE = Math.max(2, Runtime.getRuntime().availableProcessors());
    private static final int KEEP_ALIVE_TIME = 60;

    private static final ConcurrentHashMap<Long, Future<?>> TASK_MAP = new ConcurrentHashMap<>();

    public static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            CORE_SIZE,
            CORE_SIZE * 2,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000),
            new GlobalThreadFactory("global-exec"),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private GlobalThreadPool() {
    }

    /**
     * 提交任务并关联业务 ID。
     * 任务完成（正常/异常/取消）后自动从 taskMap 移除。
     */
    public static Future<?> submit(Long id, Runnable task) {
        CompletableFuture<?> future = CompletableFuture.runAsync(task, EXECUTOR);
        if (id != null) {
            TASK_MAP.put(id, future);
            future.whenComplete((v, ex) -> TASK_MAP.remove(id));
        }
        return future;
    }

    public static Future<?> getTask(Long id) {
        return TASK_MAP.get(id);
    }

    public static Future<?> setTask(Long id, Future<?> future) {
        return TASK_MAP.put(id, future);
    }

    public static Future<?> removeTask(Long id) {
        return TASK_MAP.remove(id);
    }

    public static boolean cancel(Long id) {
        Future<?> future = removeTask(id);
        if (future != null) {
            return future.cancel(true);
        }
        return false;
    }
}
