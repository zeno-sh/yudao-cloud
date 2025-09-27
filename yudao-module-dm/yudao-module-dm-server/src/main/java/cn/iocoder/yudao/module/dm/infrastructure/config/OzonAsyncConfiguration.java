package cn.iocoder.yudao.module.dm.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: Zeno
 * @createTime: 2024/10/16 15:16
 */
@Configuration(proxyBeanMethods = false)
public class OzonAsyncConfiguration {

    public static final String DM_THREAD_POOL_TASK_EXECUTOR = "DM_THREAD_POOL_TASK_EXECUTOR";

    public static final String DM_JOB_THREAD_POOL_TASK_EXECUTOR = "DM_JOB_THREAD_POOL_TASK_EXECUTOR";

    @Bean(DM_THREAD_POOL_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor notifyThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 针对2C2G环境优化线程池配置
        executor.setCorePoolSize(2); // 降低核心线程数，与CPU核心数匹配
        executor.setMaxPoolSize(4); // 降低最大线程数，避免过多线程竞争资源
        executor.setKeepAliveSeconds(120); // 延长空闲时间，减少线程创建销毁开销
        executor.setQueueCapacity(200); // 增加队列容量，缓冲请求压力
        executor.setThreadNamePrefix("DM-THREAD-POOL-TASK-"); // 配置线程池的前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 使用CallerRunsPolicy，避免任务丢失并提供反馈压力
        // 进行加载
        executor.initialize();
        return executor;
    }

    @Bean(DM_JOB_THREAD_POOL_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor jobThreadTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 针对2C2G环境优化线程池配置
        executor.setCorePoolSize(1); // 降低核心线程数
        executor.setMaxPoolSize(2); // 降低最大线程数
        executor.setQueueCapacity(150); // 增加队列容量
        executor.setThreadNamePrefix("DM-JOB-THREAD-POOL-TASK-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 使用CallerRunsPolicy提供背压
        executor.initialize();
        return executor;
    }

    @Bean("profitCalculationExecutor")
    public ThreadPoolTaskExecutor profitCalculationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 针对利润计算并行处理优化的线程池配置
        executor.setCorePoolSize(4); // 利润计算需要更多核心线程
        executor.setMaxPoolSize(8); // 最大线程数
        executor.setKeepAliveSeconds(300); // 空闲时间5分钟
        executor.setQueueCapacity(500); // 队列容量
        executor.setThreadNamePrefix("PROFIT-CALC-"); // 配置线程池的前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 使用CallerRunsPolicy，避免任务丢失
        // 进行加载
        executor.initialize();
        return executor;
    }

    public static final String DM_ORDER_ASYNC_EXECUTOR = "DM_ORDER_ASYNC_EXECUTOR";

    @Bean(DM_ORDER_ASYNC_EXECUTOR)
    @Primary
    public ThreadPoolTaskExecutor orderAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 针对订单异步同步优化的线程池配置
        executor.setCorePoolSize(3); // 核心线程数，适合并发同步多个店铺
        executor.setMaxPoolSize(6); // 最大线程数
        executor.setKeepAliveSeconds(180); // 空闲时间3分钟
        executor.setQueueCapacity(100); // 队列容量
        executor.setThreadNamePrefix("ORDER-ASYNC-"); // 配置线程池的前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 使用CallerRunsPolicy，避免任务丢失
        // 进行加载
        executor.initialize();
        return executor;
    }
}
