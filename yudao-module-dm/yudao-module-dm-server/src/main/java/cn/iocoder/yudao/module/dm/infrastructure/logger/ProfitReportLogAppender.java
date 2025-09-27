package cn.iocoder.yudao.module.dm.infrastructure.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportTaskLogService;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 财务账单报告日志拦截器
 * 用于捕获计算过程中的日志，并写入任务日志表
 *
 * @author zeno
 */
@Component
public class ProfitReportLogAppender extends AppenderBase<ILoggingEvent> {

    private static final String MDC_TASK_ID_KEY = "taskId";
    
    // 监控的包路径，只捕获这些包下的日志
    private static final String[] MONITORED_PACKAGES = {
            "cn.iocoder.yudao.module.dm.infrastructure.ozon",
            "cn.iocoder.yudao.module.dm.infrastructure.service",
            "cn.iocoder.yudao.module.dm.service.profitreport"
    };
    
    // 订单号的正则表达式
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("订单号：([^，,：:]+)");
    
    // 在线商品未映射的正则表达式
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("在线商品未映射：(\\d+)");
    
    // 日志缓存，用于去重，键为日志内容的哈希值，值为最后一次记录的时间戳
    private static final Map<String, Long> LOG_CACHE = new ConcurrentHashMap<>();
    
    // 日志去重的时间窗口，单位：毫秒（2秒内相同日志只记录一次，缩短时间以捕获更多信息）
    private static final long DEDUPLICATION_WINDOW = TimeUnit.SECONDS.toMillis(2);
    
    // 日志缓存清理周期，单位：毫秒（每30分钟清理一次过期的缓存条目）
    private static final long CACHE_CLEANUP_INTERVAL = TimeUnit.MINUTES.toMillis(30);
    
    // 上次缓存清理时间
    private static long lastCleanupTime = System.currentTimeMillis();
    
    private static ProfitReportTaskLogService taskLogService;
    
    // 当Spring上下文被初始化后，设置taskLogService
    public static void setApplicationContext(ApplicationContext applicationContext) {
        taskLogService = applicationContext.getBean(ProfitReportTaskLogService.class);
    }
    
    @Override
    protected void append(ILoggingEvent event) {
        // 如果服务未初始化，则不处理
        if (taskLogService == null) {
            return;
        }
        
        // 检查日志级别，只处理WARN, ERROR级别的日志
        if (event.getLevel().toInt() < Level.WARN_INT) {
            return;
        }
        
        // 检查是否来自被监控的包
        String loggerName = event.getLoggerName();
        boolean isMonitored = false;
        for (String pkg : MONITORED_PACKAGES) {
            if (loggerName.startsWith(pkg)) {
                isMonitored = true;
                break;
            }
        }
        
        if (!isMonitored) {
            return;
        }
        
        // 获取当前任务ID
        String taskId = MDC.get(MDC_TASK_ID_KEY);
        
        // 如果没有任务ID，则不处理
        if (StrUtil.isBlank(taskId)) {
            return;
        }
        
        // 获取日志消息
        String message = event.getFormattedMessage();
        
        // 定期清理过期的缓存条目
        cleanupCacheIfNeeded();
        
        // 生成日志的唯一标识（考虑添加任务ID前缀，确保不同任务之间不会互相影响去重）
        String logKey = taskId + ":" + generateLogKey(message);
        
        // 检查是否为重复日志
        long currentTime = System.currentTimeMillis();
        Long lastLogTime = LOG_CACHE.get(logKey);
        
        if (lastLogTime != null && currentTime - lastLogTime < DEDUPLICATION_WINDOW) {
            // 在去重时间窗口内，该日志已经记录过，跳过
            return;
        }
        
        // 更新日志缓存
        LOG_CACHE.put(logKey, currentTime);
        
        // 格式化日志信息，添加时间前缀
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date(event.getTimeStamp()));
        
        // 处理异常信息
        if (event.getThrowableProxy() != null) {
            ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
            Throwable throwable = throwableProxy.getThrowable();
            message += "\n异常: " + throwable.getClass().getName() + ": " + throwable.getMessage();
        }
        
        // 根据日志级别和内容将日志分类处理
        if (event.getLevel().toInt() >= Level.ERROR_INT) {
            // 处理错误日志
            String formattedMessage = formatLogMessage(message, timestamp, event.getLevel());
            taskLogService.appendErrorInfo(taskId, formattedMessage);
            
            // 对特定错误进行结构化处理
            handleStructuredErrors(taskId, message);
        } else if (event.getLevel().toInt() >= Level.WARN_INT) {
            // 处理警告日志
            String formattedMessage = formatLogMessage(message, timestamp, event.getLevel());
            taskLogService.appendExecuteLog(taskId, formattedMessage);
            
            // 对特定警告进行结构化处理
            handleStructuredWarnings(taskId, message);
        }
    }
    
    /**
     * 格式化日志消息
     */
    private String formatLogMessage(String message, String timestamp, Level level) {
        return String.format("[%s] [%s] %s", timestamp, level.toString(), message);
    }
    
    /**
     * 处理结构化错误信息
     */
    private void handleStructuredErrors(String taskId, String message) {
        // 只将原始消息记录到错误信息中，让统一的错误汇总机制处理去重
        taskLogService.appendErrorInfo(taskId, message);
    }
    
    /**
     * 处理结构化警告信息
     */
    private void handleStructuredWarnings(String taskId, String message) {
        // 对于警告消息，仅记录到执行日志中
        // 如果是重要的警告信息（如订单号或商品相关），也会记录到错误信息中
        if (message.contains("在线商品不存在") || message.contains("在线商品未映射")) {
            taskLogService.appendErrorInfo(taskId, message);
        }
    }
    
    /**
     * 为日志生成唯一标识
     * 对日志内容进行哈希处理，用于去重
     */
    private String generateLogKey(String message) {
        return "msg:" + message.hashCode();
    }
    
    /**
     * 定期清理过期的缓存条目
     */
    private void cleanupCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime > CACHE_CLEANUP_INTERVAL) {
            // 清理超过去重窗口时间的缓存条目
            LOG_CACHE.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > DEDUPLICATION_WINDOW);
            lastCleanupTime = currentTime;
        }
    }
} 