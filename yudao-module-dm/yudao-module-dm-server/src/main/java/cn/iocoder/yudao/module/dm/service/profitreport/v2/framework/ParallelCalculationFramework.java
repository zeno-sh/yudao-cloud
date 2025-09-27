package cn.iocoder.yudao.module.dm.service.profitreport.v2.framework;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 并行计算框架
 * 按照技术文档设计实现的分批并行处理能力
 *
 * @author Jax
 */
@Component
@Slf4j
public class ParallelCalculationFramework {
    
    @Resource
    @Qualifier("profitCalculationExecutor")
    private ThreadPoolTaskExecutor executor;
    
    // 默认批次大小
    private static final int DEFAULT_BATCH_SIZE = 1000;
    
    /**
     * 分批并行处理
     *
     * @param items 待处理的数据项
     * @param processor 处理函数
     * @param batchSize 批处理大小
     * @return 处理结果
     */
    public <T, R> List<R> processInParallel(
            List<T> items, 
            Function<List<T>, List<R>> processor,
            int batchSize) {
        
        if (items == null || items.isEmpty()) {
            return Lists.newArrayListWithCapacity(0);
        }
        
        log.debug("开始并行处理: 总数据量={}, 批大小={}", items.size(), batchSize);
        
        // 分批处理
        List<List<T>> batches = partitionList(items, batchSize);
        
        // 并行执行
        List<CompletableFuture<List<R>>> futures = batches.stream()
            .map(batch -> CompletableFuture.supplyAsync(() -> processor.apply(batch), executor))
            .collect(Collectors.toList());
        
        // 收集结果
        List<R> results = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        log.debug("并行处理完成: 分批数={}, 结果数量={}", batches.size(), results.size());
        
        return results;
    }
    
    /**
     * 并行执行多个独立任务
     *
     * @param tasks 任务列表
     * @return 执行结果
     */
    public <R> List<R> executeInParallel(List<CompletableFuture<R>> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Lists.newArrayListWithCapacity(0);
        }
        
        log.debug("开始并行执行: 任务数={}", tasks.size());
        
        List<R> results = tasks.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        
        log.debug("并行执行完成: 结果数量={}", results.size());
        
        return results;
    }
    
    /**
     * 创建异步任务
     *
     * @param supplier 任务供应商
     * @return CompletableFuture
     */
    public <R> CompletableFuture<R> supplyAsync(java.util.function.Supplier<R> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }
    
    /**
     * 分割列表为指定大小的批次
     */
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("批大小必须大于0");
        }
        
        List<List<T>> batches = new java.util.ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, end));
        }
        return batches;
    }
    
    /**
     * 分批并行处理（使用默认批次大小）
     */
    public <T, R> List<R> processInParallel(List<T> items, Function<List<T>, List<R>> processor) {
        return processInParallel(items, processor, DEFAULT_BATCH_SIZE);
    }
    
    /**
     * 并行执行多个不同的任务
     *
     * @param tasks 任务列表
     * @param <R>   结果类型
     * @return 所有任务的结果
     */
    @SafeVarargs
    public final <R> List<R> executeTasksInParallel(CompletableFuture<R>... tasks) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("开始执行并行任务: 任务数={}", tasks.length);
        
        try {
            // 等待所有任务完成
            CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks);
            allOf.join();
            
            // 收集结果
            List<R> results = Lists.newArrayListWithCapacity(tasks.length);
            for (CompletableFuture<R> task : tasks) {
                results.add(task.join());
            }
            
            Duration duration = Duration.between(startTime, LocalDateTime.now());
            log.info("并行任务执行完成: 任务数={}, 耗时={}ms", tasks.length, duration.toMillis());
            
            return results;
            
        } catch (Exception e) {
            log.error("并行任务执行失败: 任务数={}", tasks.length, e);
            throw new RuntimeException("并行任务执行失败", e);
        }
    }
    
    /**
     * 自适应批次大小处理
     * 根据数据量和系统资源动态调整批次大小
     */
    public <T, R> List<R> processWithAdaptiveBatch(
            List<T> items, 
            Function<List<T>, List<R>> processor) {
        
        if (items == null || items.isEmpty()) {
            return Lists.newArrayList();
        }
        
        // 根据数据量和线程池状态计算最优批次大小
        int optimalBatchSize = calculateOptimalBatchSize(items.size());
        
        log.info("自适应批次处理: 数据量={}, 最优批次大小={}", items.size(), optimalBatchSize);
        
        return processInParallel(items, processor, optimalBatchSize);
    }
    
    /**
     * 计算最优批次大小
     */
    private int calculateOptimalBatchSize(int totalSize) {
        // 获取线程池状态
        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();
        int corePoolSize = threadPool.getCorePoolSize();
        int activeCount = threadPool.getActiveCount();
        
        // 基础批次大小：确保每个核心线程至少有一个批次
        int baseBatchSize = Math.max(totalSize / (corePoolSize * 2), 100);
        
        // 根据活跃线程数调整
        if (activeCount > corePoolSize * 0.8) {
            // 系统繁忙时增大批次大小，减少任务调度开销
            baseBatchSize = (int) (baseBatchSize * 1.5);
        }
        
        // 边界检查
        int minBatchSize = 50;
        int maxBatchSize = 5000;
        
        return Math.max(minBatchSize, Math.min(maxBatchSize, baseBatchSize));
    }
    
    /**
     * 监控线程池状态
     */
    public ThreadPoolStatus getThreadPoolStatus() {
        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();
        
        return ThreadPoolStatus.builder()
                .corePoolSize(threadPool.getCorePoolSize())
                .maximumPoolSize(threadPool.getMaximumPoolSize())
                .activeCount(threadPool.getActiveCount())
                .poolSize(threadPool.getPoolSize())
                .queueSize(threadPool.getQueue().size())
                .completedTaskCount(threadPool.getCompletedTaskCount())
                .taskCount(threadPool.getTaskCount())
                .isShutdown(threadPool.isShutdown())
                .isTerminated(threadPool.isTerminated())
                .build();
    }
    
    /**
     * 线程池状态信息
     */
    @lombok.Data
    @lombok.Builder
    public static class ThreadPoolStatus {
        private int corePoolSize;
        private int maximumPoolSize;
        private int activeCount;
        private int poolSize;
        private int queueSize;
        private long completedTaskCount;
        private long taskCount;
        private boolean isShutdown;
        private boolean isTerminated;
        
        /**
         * 计算线程池负载率
         */
        public double getLoadRate() {
            return (double) activeCount / maximumPoolSize;
        }
        
        /**
         * 计算任务完成率
         */
        public double getCompletionRate() {
            return taskCount > 0 ? (double) completedTaskCount / taskCount : 0.0;
        }
        
        /**
         * 判断是否过载
         */
        public boolean isOverloaded() {
            return getLoadRate() > 0.8 && queueSize > corePoolSize;
        }
    }
} 