package com.zippy.kit.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程工具类：全局默认线程池 + 池状态快照。
 *
 * <pre>
 * // 使用全局默认池
 * ExecutorService pool = Threads.globalPool();
 *
 * // 查看池状态
 * Threads.PoolStatus status = Threads.status(pool);
 * System.out.println(status.getActiveThreads());
 * </pre>
 */
public final class Threads {

    private static volatile ExecutorService globalPool;

    private Threads() {
    }

    /**
     * 获取全局默认线程池（懒加载）。
     * <p>
     * 使用 {@link ThreadPoolBuilder} 的默认配置创建。
     * 如需自定义，请使用 {@code ThreadPoolBuilder.create()...build()} 创建独立实例。
     */
    public static ExecutorService globalPool() {
        if (globalPool == null) {
            synchronized (Threads.class) {
                if (globalPool == null) {
                    globalPool = ThreadPoolBuilder.create()
                            .threadPrefix("global")
                            .build();
                }
            }
        }
        return globalPool;
    }

    /**
     * 获取线程池状态快照。
     * <p>
     * 如果 Executor 不是 {@link ThreadPoolExecutor}，返回的状态字段为 -1。
     */
    public static PoolStatus status(ExecutorService executor) {
        if (executor instanceof ThreadPoolExecutor pool) {
            return new PoolStatus(
                    pool.getPoolSize(),
                    pool.getActiveCount(),
                    pool.getCorePoolSize(),
                    pool.getMaximumPoolSize(),
                    pool.getQueue().size(),
                    pool.getQueue().remainingCapacity(),
                    pool.getCompletedTaskCount(),
                    pool.getTaskCount()
            );
        }
        return PoolStatus.UNKNOWN;
    }

    /**
     * 线程池状态快照。
     */
    public record PoolStatus(
            int poolSize,
            int activeThreads,
            int coreSize,
            int maxSize,
            int queueSize,
            int queueRemainingCapacity,
            long completedTasks,
            long totalTasks
    ) {
        static final PoolStatus UNKNOWN = new PoolStatus(-1, -1, -1, -1, -1, -1, -1, -1);

        /**
         * 队列使用率（0.0 ~ 1.0），未知时返回 -1。
         */
        public double queueUsage() {
            if (queueSize < 0 || queueRemainingCapacity < 0) {
                return -1;
            }
            int total = queueSize + queueRemainingCapacity;
            return total == 0 ? 0.0 : (double) queueSize / total;
        }

        @Override
        public String toString() {
            if (this == UNKNOWN) {
                return "PoolStatus{unknown}";
            }
            return "PoolStatus{pool=%d, active=%d, core=%d, max=%d, queue=%d/%d, completed=%d, total=%d}"
                    .formatted(poolSize, activeThreads, coreSize, maxSize,
                            queueSize, queueSize + queueRemainingCapacity,
                            completedTasks, totalTasks);
        }
    }
}
