package cn.iocoder.yudao.module.dm.infrastructure.ozon;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.ProfitReportTaskStatusEnum;
import cn.iocoder.yudao.module.dm.infrastructure.service.ProfitComputeService;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportService;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportTaskLogService;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.dm.infrastructure.config.OzonAsyncConfiguration.DM_THREAD_POOL_TASK_EXECUTOR;

/**
 * @author: Zeno
 * @createTime: 2024/10/16 16:23
 */
@Service
public class ProfitComputeManagerService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfitComputeManagerService.class);
    // 任务ID的MDC键
    private static final String MDC_TASK_ID_KEY = "taskId";
    // 批处理大小 - 客户端数量阈值，超过此阈值将拆分成多批处理
    private static final int CLIENT_BATCH_SIZE = 5;
    // 日期范围阈值（天数），超过此阈值将拆分成多批处理
    private static final int DATE_RANGE_THRESHOLD = 31;
    // 批处理内部处理大小
    private static final int INTERNAL_BATCH_SIZE = 10;

    // 限流器 - 每秒允许的操作数，默认为1
    @Value("${dm.profit.compute.rate:1.0}")
    private double computeRatePerSecond;
    
    // 使用Google Guava的RateLimiter进行更优雅的限流控制
    private RateLimiter rateLimiter;

    @Resource
    private ProfitComputeService profitComputeService;
    @Resource
    private ProfitReportService profitReportService;
    @Resource
    private ProfitReportTaskLogService taskLogService;
    
    public ProfitComputeManagerService() {
        // 默认初始化，会被属性注入覆盖
        this.rateLimiter = RateLimiter.create(1.0);
    }
    
    /**
     * 初始化RateLimiter，在所有属性设置完成后调用
     */
    public void initRateLimiter() {
        this.rateLimiter = RateLimiter.create(computeRatePerSecond);
        logger.info("利润计算限流器初始化完成，速率: {} 次/秒", computeRatePerSecond);
    }

    @Async(DM_THREAD_POOL_TASK_EXECUTOR)
    public void handleComputeProfit(List<String> clientIds, String beginDate, String endDate) {
        handleComputeProfit(null, clientIds, beginDate, endDate, null, null);
    }
    
    @Async(DM_THREAD_POOL_TASK_EXECUTOR)
    public void handleComputeProfit(String taskId, List<String> clientIds, String beginDate, String endDate, 
                                     String dimension, String timeType) {
        // 参数验证
        if (!validateParameters(clientIds, beginDate, endDate)) {
            return;
        }
        
        String finalTaskId = null;
        try {
            // 初始化限流器
            ensureRateLimiterInitialized();
            
            // 解析日期
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(beginDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            
            // 创建或获取任务日志
            finalTaskId = getOrCreateTaskId(taskId, clientIds, dimension, timeType, start, end);
            
            // 设置MDC上下文
            MDC.put(MDC_TASK_ID_KEY, finalTaskId);
            
            // 执行利润计算任务
            executeComputeTask(finalTaskId, clientIds, start, end, formatter);
            
        } catch (Exception e) {
            handleTaskException(finalTaskId, e);
        } finally {
            // 清理MDC上下文
            MDC.remove(MDC_TASK_ID_KEY);
        }
    }
    
    /**
     * 参数验证
     */
    private boolean validateParameters(List<String> clientIds, String beginDate, String endDate) {
        if (clientIds == null || clientIds.isEmpty()) {
            logger.error("客户端ID列表不能为空");
            return false;
        }
        
        if (beginDate == null || endDate == null) {
            logger.error("开始日期和结束日期不能为空");
            return false;
        }
        
        try {
            LocalDate start = LocalDate.parse(beginDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            if (start.isAfter(end)) {
                logger.error("开始日期不能晚于结束日期: {} > {}", beginDate, endDate);
                return false;
            }
        } catch (Exception e) {
            logger.error("日期格式错误，应为 yyyy-MM-dd 格式: beginDate={}, endDate={}", beginDate, endDate);
            return false;
        }
        
        return true;
    }
    
    /**
     * 确保限流器已初始化
     */
    private void ensureRateLimiterInitialized() {
        if (rateLimiter == null) {
            initRateLimiter();
        }
    }
    
    /**
     * 获取或创建任务ID
     */
    private String getOrCreateTaskId(String taskId, List<String> clientIds, String dimension, 
                                   String timeType, LocalDate start, LocalDate end) {
        if (taskId != null) {
            return taskId;
        }
        
        return taskLogService.createTaskLog(
            clientIds.toArray(new String[0]), 
            dimension, 
            timeType, 
            start, 
            end
        );
    }
    
    /**
     * 执行计算任务的核心逻辑
     */
    private void executeComputeTask(String taskId, List<String> clientIds, LocalDate start, LocalDate end, 
                                  DateTimeFormatter formatter) {
        // 记录任务开始
        logTaskStart(taskId, clientIds, start, end);
        
        // 删除历史数据
        deleteHistoricalData(taskId, clientIds, start, end);
        
        // 计算处理策略并执行
        ProcessingResult result = executeProcessingStrategy(taskId, clientIds, start, end, formatter);
        
        // 更新任务状态
        updateTaskStatus(taskId, result);
    }
    
    /**
     * 记录任务开始日志
     */
    private void logTaskStart(String taskId, List<String> clientIds, LocalDate start, LocalDate end) {
        String logMsg = String.format("开始计算任务，客户端数量: %d, 时间范围: %s 至 %s", 
            clientIds.size(), start, end);
        logger.info(logMsg);
        taskLogService.appendExecuteLog(taskId, logMsg);
    }
    
    /**
     * 删除历史数据
     */
    private void deleteHistoricalData(String taskId, List<String> clientIds, LocalDate start, LocalDate end) {
        try {
            taskLogService.appendExecuteLog(taskId, "正在删除历史数据...");
            deleteProfitReport(clientIds, start, end);
            taskLogService.appendExecuteLog(taskId, "历史数据删除完成");
        } catch (Exception e) {
            String errorMsg = "删除历史数据出错: " + e.getMessage();
            taskLogService.appendErrorInfo(taskId, errorMsg);
            taskLogService.appendExecuteLog(taskId, errorMsg);
            logger.error(errorMsg, e);
            throw new RuntimeException("删除历史数据失败", e);
        }
    }
    
    /**
     * 执行处理策略
     */
    private ProcessingResult executeProcessingStrategy(String taskId, List<String> clientIds, 
                                                     LocalDate start, LocalDate end, DateTimeFormatter formatter) {
        long daysBetween = ChronoUnit.DAYS.between(start, end) + 1;
        AtomicInteger processedRecords = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
                 try {
             if (daysBetween > DATE_RANGE_THRESHOLD) {
                 // 日期范围较大，按周分段处理
                 String logMsg = String.format("日期范围较大(%d天，阈值:%d)，采用分段处理策略", daysBetween, DATE_RANGE_THRESHOLD);
                 logger.info(logMsg);
                 taskLogService.appendExecuteLog(taskId, logMsg);
                 processLargeDateRange(taskId, clientIds, start, end, formatter, processedRecords, errorCount);
             } else if (clientIds.size() > CLIENT_BATCH_SIZE) {
                 // 客户端数量较多，分批处理
                 String logMsg = String.format("客户端数量较多(%d个，阈值:%d)，采用分批处理策略", clientIds.size(), CLIENT_BATCH_SIZE);
                 logger.info(logMsg);
                 taskLogService.appendExecuteLog(taskId, logMsg);
                 processLargeClientBatch(taskId, clientIds, start, end, formatter, processedRecords, errorCount);
             } else {
                 // 正常处理
                 String logMsg = "使用标准处理策略";
                 logger.info(logMsg);
                 taskLogService.appendExecuteLog(taskId, logMsg);
                 processNormally(taskId, clientIds, start, end, formatter, processedRecords, errorCount);
             }
            
            return new ProcessingResult(processedRecords.get(), errorCount.get());
            
        } catch (Exception e) {
            String errorMsg = "财务账单报告计算出错: " + e.getMessage();
            logger.error(errorMsg, e);
            taskLogService.appendErrorInfo(taskId, errorMsg);
            taskLogService.appendExecuteLog(taskId, errorMsg);
            throw e;
        }
    }
    
    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String taskId, ProcessingResult result) {
        if (result.getErrorCount() > 0) {
            String logMsg = String.format("任务完成，但有 %d 个错误，共处理 %d 条记录", 
                result.getErrorCount(), result.getProcessedCount());
            taskLogService.appendExecuteLog(taskId, logMsg);
            taskLogService.updateTaskStatus(taskId, ProfitReportTaskStatusEnum.FAILED, 
                null, null, result.getProcessedCount());
        } else {
            String logMsg = String.format("任务成功完成，共处理 %d 条记录", result.getProcessedCount());
            taskLogService.appendExecuteLog(taskId, logMsg);
            taskLogService.updateTaskStatus(taskId, ProfitReportTaskStatusEnum.SUCCESS, 
                null, null, result.getProcessedCount());
        }
    }
    
    /**
     * 处理任务异常
     */
    private void handleTaskException(String taskId, Exception e) {
        String errorMsg = "利润计算任务执行失败: " + e.getMessage();
        logger.error(errorMsg, e);
        
        if (taskId != null) {
            taskLogService.appendErrorInfo(taskId, errorMsg);
            taskLogService.appendExecuteLog(taskId, errorMsg);
            taskLogService.updateTaskStatus(taskId, ProfitReportTaskStatusEnum.FAILED, null, null, 0);
        }
    }
    
    /**
     * 处理结果封装类
     */
    private static class ProcessingResult {
        private final int processedCount;
        private final int errorCount;
        
        public ProcessingResult(int processedCount, int errorCount) {
            this.processedCount = processedCount;
            this.errorCount = errorCount;
        }
        
        public int getProcessedCount() {
            return processedCount;
        }
        
        public int getErrorCount() {
            return errorCount;
        }
    }

    // 处理较大日期范围的情况，按周分段处理
    private void processLargeDateRange(String taskId, List<String> clientIds, LocalDate start, LocalDate end, 
                                       DateTimeFormatter formatter, AtomicInteger processedRecords, AtomicInteger errorCount) {
        LocalDate currentStart = start;
        int segmentIndex = 1;
        int totalSegments = (int) Math.ceil(ChronoUnit.DAYS.between(start, end) * 1.0 / DATE_RANGE_THRESHOLD);
        
        while (currentStart.isBefore(end) || currentStart.isEqual(end)) {
            // 计算当前分段的结束日期
            LocalDate currentEnd = currentStart.plusDays(DATE_RANGE_THRESHOLD - 1);
            if (currentEnd.isAfter(end)) {
                currentEnd = end;
            }
            
            // 处理当前日期段
            String logMsg = "处理日期段 " + segmentIndex + "/" + totalSegments + ": " + 
                currentStart.format(formatter) + " 至 " + currentEnd.format(formatter);
            logger.info(logMsg);
            taskLogService.appendExecuteLog(taskId, logMsg);
            
            try {
                processNormally(taskId, clientIds, currentStart, currentEnd, formatter, processedRecords, errorCount);
            } catch (Exception e) {
                String errorMsg = "处理日期段 " + segmentIndex + "/" + totalSegments + " 出错: " + e.getMessage();
                logger.error(errorMsg, e);
                taskLogService.appendErrorInfo(taskId, errorMsg);
                taskLogService.appendExecuteLog(taskId, errorMsg);
                errorCount.incrementAndGet();
            }
            
            // 移动到下一个日期段
            currentStart = currentEnd.plusDays(1);
            segmentIndex++;
            
            // 使用限流器控制处理速率，更优雅地节流
            rateLimiter.acquire();
        }
    }
    
    // 处理较多客户端数量的情况，分批处理
    private void processLargeClientBatch(String taskId, List<String> clientIds, LocalDate start, LocalDate end, 
                                         DateTimeFormatter formatter, AtomicInteger processedRecords, AtomicInteger errorCount) {
        int totalClients = clientIds.size();
        int batchCount = (totalClients + CLIENT_BATCH_SIZE - 1) / CLIENT_BATCH_SIZE; // 向上取整
        
        for (int i = 0; i < batchCount; i++) {
            int fromIndex = i * CLIENT_BATCH_SIZE;
            int toIndex = Math.min(fromIndex + CLIENT_BATCH_SIZE, totalClients);
            
            List<String> batchClients = clientIds.subList(fromIndex, toIndex);
            String logMsg = "处理客户端批次 " + (i+1) + "/" + batchCount + ", 包含 " + batchClients.size() + " 个客户端";
            logger.info(logMsg);
            taskLogService.appendExecuteLog(taskId, logMsg);
            
            try {
                processNormally(taskId, batchClients, start, end, formatter, processedRecords, errorCount);
            } catch (Exception e) {
                String errorMsg = "处理客户端批次 " + (i+1) + "/" + batchCount + " 出错: " + e.getMessage();
                logger.error(errorMsg, e);
                taskLogService.appendErrorInfo(taskId, errorMsg);
                taskLogService.appendExecuteLog(taskId, errorMsg);
                errorCount.incrementAndGet();
            }
            
            // 使用限流器控制处理速率，更优雅地节流
            rateLimiter.acquire();
        }
    }
    
    // 正常处理逻辑，对全部日期和全部客户端进行批处理
    private void processNormally(String taskId, List<String> clientIds, LocalDate start, LocalDate end, 
                                 DateTimeFormatter formatter, AtomicInteger processedRecords, AtomicInteger errorCount) {
        // 创建一个集合用于批处理，避免频繁的单次处理
        List<ClientDatePair> batchPairs = new ArrayList<>();
        int batchSize = 0;
        int totalPairs = clientIds.size() * (int)(ChronoUnit.DAYS.between(start, end) + 1);
        int processedPairs = 0;
        
        // 遍历从开始日期到结束日期的每一天
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateStr = DateUtil.format(date.atStartOfDay(), DatePattern.NORM_DATE_PATTERN);
            
            // 按日期和客户端组合处理，收集到批处理队列中
            for (String clientId : clientIds) {
                batchPairs.add(new ClientDatePair(clientId, dateStr));
                batchSize++;
                
                // 当批处理大小达到阈值时，一次性提交批处理
                if (batchSize >= INTERNAL_BATCH_SIZE) {
                    executeBatch(taskId, batchPairs, processedRecords, errorCount);
                    processedPairs += batchPairs.size();
                    
                    String progressMsg = "处理进度: " + processedPairs + "/" + totalPairs + 
                                         " (" + (processedPairs * 100 / totalPairs) + "%)";
                    taskLogService.appendExecuteLog(taskId, progressMsg);
                    
                    batchPairs.clear();
                    batchSize = 0;
                    
                    // 对批处理操作进行限流
                    rateLimiter.acquire();
                }
            }
        }
        
        // 处理剩余的批处理项
        if (!batchPairs.isEmpty()) {
            executeBatch(taskId, batchPairs, processedRecords, errorCount);
            processedPairs += batchPairs.size();
            
            String progressMsg = "处理进度: " + processedPairs + "/" + totalPairs + 
                               " (" + (processedPairs * 100 / totalPairs) + "%)";
            taskLogService.appendExecuteLog(taskId, progressMsg);
        }
    }
    
    /**
     * 执行批处理 - 优化版本，支持真正的批量处理
     */
    private void executeBatch(String taskId, List<ClientDatePair> batchPairs, 
                             AtomicInteger processedRecords, AtomicInteger errorCount) {
        if (batchPairs.isEmpty()) {
            return;
        }
        
        // 按日期分组，实现真正的批量处理
        Map<String, List<String>> dateToClientsMap = batchPairs.stream()
            .collect(Collectors.groupingBy(
                ClientDatePair::getDate,
                Collectors.mapping(ClientDatePair::getClientId, Collectors.toList())
            ));
        
        // 按日期批量处理
        for (Map.Entry<String, List<String>> entry : dateToClientsMap.entrySet()) {
            String date = entry.getKey();
            List<String> clientIds = entry.getValue();
            
            try {
                // 一次性处理同一日期的所有客户端
                profitComputeService.computeProfitReport(date, clientIds);
                processedRecords.addAndGet(clientIds.size());
                
                if (logger.isDebugEnabled()) {
                    logger.debug("成功处理日期[{}]的{}个客户端", date, clientIds.size());
                }
            } catch (Exception e) {
                String errorMsg = String.format("处理日期[%s]的客户端数据出错，客户端数量: %d, 错误: %s", 
                    date, clientIds.size(), e.getMessage());
                logger.error(errorMsg, e);
                taskLogService.appendErrorInfo(taskId, errorMsg);
                errorCount.addAndGet(clientIds.size());
                
                // 如果批量处理失败，尝试单个处理以确定具体失败的客户端
                handleBatchFailure(taskId, date, clientIds, processedRecords, errorCount);
            }
        }
    }
    
    /**
     * 处理批量失败的情况，逐个重试以确定具体失败的客户端
     */
    private void handleBatchFailure(String taskId, String date, List<String> clientIds, 
                                   AtomicInteger processedRecords, AtomicInteger errorCount) {
        logger.warn("批量处理失败，开始逐个重试，日期: {}, 客户端数量: {}", date, clientIds.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (String clientId : clientIds) {
            try {
                profitComputeService.computeProfitReport(date, Lists.newArrayList(clientId));
                successCount++;
            } catch (Exception e) {
                String errorMsg = String.format("单独处理客户端[%s]日期[%s]数据出错: %s", 
                    clientId, date, e.getMessage());
                logger.error(errorMsg, e);
                taskLogService.appendErrorInfo(taskId, errorMsg);
                failureCount++;
            }
        }
        
        // 调整计数器（之前已经按批量失败计算了，现在需要调整）
        processedRecords.addAndGet(successCount);
        errorCount.addAndGet(failureCount - clientIds.size()); // 减去之前批量计算的错误数
        
        logger.info("逐个重试完成，日期: {}, 成功: {}, 失败: {}", date, successCount, failureCount);
    }

    private void deleteProfitReport(List<String> clientIds, LocalDate start, LocalDate end){
        LocalDate[] financeDate = new LocalDate[]{start, end};
        profitReportService.deleteProfitReport(clientIds.toArray(new String[0]), financeDate);
    }
    
    // 客户端和日期的配对类，用于批处理
    private static class ClientDatePair {
        private final String clientId;
        private final String date;
        
        public ClientDatePair(String clientId, String date) {
            this.clientId = clientId;
            this.date = date;
        }
        
        public String getClientId() {
            return clientId;
        }
        
        public String getDate() {
            return date;
        }
    }
}