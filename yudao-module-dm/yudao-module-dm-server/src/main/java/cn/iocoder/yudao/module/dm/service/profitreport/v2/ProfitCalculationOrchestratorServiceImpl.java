package cn.iocoder.yudao.module.dm.service.profitreport.v2;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.*;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.calculation.CalculationEngine;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.data.DataCollector;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.data.DataAggregator;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.task.TaskManager;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.*;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.storage.ProfitReportV2StorageService;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportTaskLogService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.ProfitReportTaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.util.ProfitCalculationLogger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.List;

/**
 * 财务利润计算业务编排器实现
 * 负责协调整个计算流程
 *
 * @author Jax
 */
@Service
@Slf4j
public class ProfitCalculationOrchestratorServiceImpl implements ProfitCalculationOrchestratorService {
    
    @Resource
    private DataCollector dataCollector;
    @Resource
    private CalculationEngine calculationEngine;
    @Resource
    private DataAggregator dataAggregator;
    @Resource
    private TaskManager taskManager;
    @Resource
    private ProfitReportV2StorageService storageService;
    @Resource
    private ProfitReportTaskLogService profitReportTaskLogService;
    @Resource
    private ProfitCalculationLogger profitLogger;
    
    @Override
    public TaskInitResultVO initializeAndStartCalculation(ProfitCalculationRequestVO request) {
        // 1. 解析clientId和日期范围
        String[] clientIds = request.getClientIds();
        String clientId = (clientIds != null && clientIds.length > 0) ? clientIds[0] : null;
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (request.getFinanceDate() != null && request.getFinanceDate().length == 2) {
            startDate = LocalDate.parse(request.getFinanceDate()[0]);
            endDate = LocalDate.parse(request.getFinanceDate()[1]);
        }
        
        // 2. 删除历史数据（按clientId和日期范围）
        if (clientId != null && startDate != null && endDate != null) {
            storageService.deleteResultsByClientIdAndDate(clientId, startDate, endDate);
        }
        
        // 3. 创建任务日志，获取taskId（参照V1版本方式）
        String taskId = profitReportTaskLogService.createTaskLog(
                new String[]{clientId},
                request.getDimension(),
                request.getTimeType(),
                startDate,
                endDate
        );
        
        // 4. 立即异步执行计算（参照V1版本方式）
        executeCalculationAsync(request, taskId);
        
        // 5. 构建计算范围描述
        String calculationScope = buildCalculationScope(request);
        
        // 6. 预估处理时长
        Integer estimatedDuration = estimateProcessingTime(request);
        
        // 7. 创建任务初始化结果
        TaskInitResultVO result = TaskInitResultVO.builder()
                .taskId(taskId)
                .status(0) // 初始状态：待处理
                .estimatedDuration(estimatedDuration)
                .createTime(LocalDateTime.now())
                .calculationScope(calculationScope)
                .build();
        
        log.info("利润计算任务初始化并启动完成: taskId={}, scope={}", taskId, calculationScope);
        return result;
    }
    
    @Override
    public PageResult<ProfitReportResultVO> getCalculationResult(ProfitReportQueryReqVO queryReqVO) {
        log.info("查询利润计算结果: taskId={}, clientId={}", queryReqVO.getTaskId(), queryReqVO.getClientId());
        
        try {
            // 1. 根据查询条件从结果表获取数据
            PageResult<ProfitReportResultVO> pageResult = storageService.getResultsByPage(queryReqVO);
            
            log.info("查询利润计算结果完成: 总记录数={}, 当前页记录数={}", 
                    pageResult.getTotal(), pageResult.getList().size());
            
            return pageResult;
            
        } catch (Exception e) {
            log.error("查询利润计算结果失败: taskId={}, clientId={}", 
                    queryReqVO.getTaskId(), queryReqVO.getClientId(), e);
            return PageResult.empty();
        }
    }
    
    @Override
    public void exportCalculationResult(ProfitReportQueryReqVO queryReqVO, HttpServletResponse response) {
        log.info("导出利润计算结果: taskId={}, clientId={}", queryReqVO.getTaskId(), queryReqVO.getClientId());
        
        try {
            // TODO: 导出功能待实现
            throw new UnsupportedOperationException("导出功能尚未实现");
            
        } catch (Exception e) {
            log.error("导出利润计算结果失败: taskId={}, clientId={}", 
                    queryReqVO.getTaskId(), queryReqVO.getClientId(), e);
            throw new RuntimeException("导出失败", e);
        }
    }
    
    @Override
    public Boolean cancelCalculation(String taskId) {
        log.info("取消利润计算任务: taskId={}", taskId);
        
        return taskManager.cancelTask(taskId);
    }
    
    /**
     * 异步执行利润计算（兼容V1版本的参数形式）
     */
    @Override
    public void executeCalculationAsyncV1Compatible(String taskId, List<String> clientIds, 
                                       String startDate, String endDate, 
                                       String dimension, String timeType) {
        
        log.info("V2版本异步执行利润计算: taskId={}, 门店={}, 日期={}-{}, 维度={}, 时间类型={}", 
                taskId, String.join(",", clientIds), startDate, endDate, dimension, timeType);
        
        // 构建请求参数
        ProfitCalculationRequestVO request = new ProfitCalculationRequestVO();
        request.setClientIds(clientIds.toArray(new String[0]));
        request.setFinanceDate(new String[]{startDate, endDate});
        request.setDimension(dimension != null ? dimension : "client");
        request.setTimeType(timeType != null ? timeType : "day");
        
        // 调用改进后的异步执行方法
        executeCalculationAsyncImproved(request, taskId);
    }

    /**
     * 异步执行利润计算（原有方法，保持兼容性）
     */
    @Async("profitCalculationExecutor")
    public void executeCalculationAsync(ProfitCalculationRequestVO request, String taskId) {
        // 直接调用改进后的方法
        executeCalculationAsyncImproved(request, taskId);
    }

    /**
     * 异步执行利润计算（改进版，优化日志输出）
     */
    @Async("profitCalculationExecutor")
    public CompletableFuture<ProfitCalculationResult> executeCalculationAsyncImproved(
            ProfitCalculationRequestVO request, String taskId) {
        
        // 任务ID常量
        final String MDC_TASK_ID_KEY = "taskId";
        
        try {
            // 设置MDC上下文
            MDC.put(MDC_TASK_ID_KEY, taskId);
            
            // 记录任务开始（简化日志）
            String clientIds = request.getClientIds() != null ? String.join(",", request.getClientIds()) : "";
            String dateRange = request.getFinanceDate() != null && request.getFinanceDate().length >= 2 
                ? request.getFinanceDate()[0] + " 至 " + request.getFinanceDate()[1] : "";
            
            profitLogger.logImportantInfo(taskId, "开始财务账单报告计算 - 门店: %s, 日期: %s, 维度: %s", 
                clientIds, dateRange, request.getDimension());
            
            // 1. 任务初始化
            profitLogger.logProgress(taskId, "初始化计算任务...");
            try {
                taskManager.initializeTask(taskId, request);
                profitLogger.logSuccess(taskId, "计算任务初始化完成");
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "计算任务初始化失败", e);
                throw e;
            }
            
            // 2. 删除历史数据（按照V1版本的逻辑）
            try {
                profitLogger.logProgress(taskId, "开始删除历史数据...");
                deleteHistoricalData(taskId, request);
                profitLogger.logSuccess(taskId, "历史数据删除完成");
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "删除历史数据失败", e);
                throw e;
            }
            
            // 3. 数据收集阶段
            DataCollectionResult dataResult;
            try {
                profitLogger.logProgress(taskId, "开始数据收集阶段...");
                
                dataResult = dataCollector.collectData(request, taskId);
                taskManager.updateProgress(taskId, TaskPhase.DATA_COLLECTION_COMPLETED, 30);
                
                // 记录数据收集统计（突出重要信息）
                profitLogger.logDataCollectionStats(taskId, "订单数据", 
                    dataResult.getOrderData() != null && dataResult.getOrderData().getSignedOrders() != null 
                        ? dataResult.getOrderData().getSignedOrders().size() : 0,
                    dataResult.getOrderData() != null && dataResult.getOrderData().getSignedOrders() != null 
                        ? dataResult.getOrderData().getSignedOrders().size() : 0);
                
                profitLogger.logDataCollectionStats(taskId, "财务交易数据", 
                    dataResult.getCostData() != null && dataResult.getCostData().getFinanceTransactions() != null 
                        ? dataResult.getCostData().getFinanceTransactions().size() : 0,
                    dataResult.getCostData() != null && dataResult.getCostData().getFinanceTransactions() != null 
                        ? dataResult.getCostData().getFinanceTransactions().size() : 0);
                
                profitLogger.logDataCollectionStats(taskId, "产品数据", 
                    dataResult.getProductData() != null && dataResult.getProductData().getProductPurchases() != null 
                        ? dataResult.getProductData().getProductPurchases().size() : 0,
                    dataResult.getProductData() != null && dataResult.getProductData().getProductPurchases() != null 
                        ? dataResult.getProductData().getProductPurchases().size() : 0);
                
                // 检查汇率数据缺失（重要警告）
                if (dataResult.getExchangeRateData() == null || 
                    dataResult.getExchangeRateData().getExchangeRates() == null ||
                    dataResult.getExchangeRateData().getExchangeRates().isEmpty()) {
                    profitLogger.logExchangeRateMissing(taskId, "多币种", dateRange);
                }
                
                profitLogger.logSuccess(taskId, "数据收集阶段完成");
                
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "数据收集阶段失败", e);
                throw e;
            }
            
            // 4. 成本计算阶段
            CalculationResult calculationResult;
            try {
                profitLogger.logProgress(taskId, "开始成本计算阶段...");
                
                calculationResult = calculationEngine.calculate(dataResult, taskId);
                taskManager.updateProgress(taskId, TaskPhase.CALCULATION_COMPLETED, 70);
                
                // 记录计算结果统计
                profitLogger.logBatchProcessingStats(taskId, "成本计算", 
                    calculationResult.getCostDetails() != null ? calculationResult.getCostDetails().size() : 0,
                    calculationResult.getCostDetails() != null ? calculationResult.getCostDetails().size() : 0,
                    calculationResult.getExceptions() != null ? calculationResult.getExceptions().size() : 0);
                
                // 记录计算过程中的异常信息（重要警告）
                if (calculationResult.getExceptions() != null && !calculationResult.getExceptions().isEmpty()) {
                    for (String exception : calculationResult.getExceptions()) {
                        profitLogger.logStructuredWarning(taskId, "计算过程异常: %s", exception);
                    }
                }
                
                profitLogger.logSuccess(taskId, "成本计算阶段完成 - 成本明细: %d 条, 利润明细: %d 条",
                    calculationResult.getCostDetails() != null ? calculationResult.getCostDetails().size() : 0,
                    calculationResult.getProfitDetails() != null ? calculationResult.getProfitDetails().size() : 0);
                
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "成本计算阶段失败", e);
                throw e;
            }
            
            // 5. 数据聚合阶段
            AggregationResult aggregationResult;
            try {
                profitLogger.logProgress(taskId, "开始数据聚合阶段...");
                
                aggregationResult = dataAggregator.aggregate(calculationResult, request, taskId);
                taskManager.updateProgress(taskId, TaskPhase.AGGREGATION_COMPLETED, 90);
                
                profitLogger.logSuccess(taskId, "数据聚合阶段完成 - 聚合记录: %d 条",
                    aggregationResult.getTotalRecords() != null ? aggregationResult.getTotalRecords() : 0);
                
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "数据聚合阶段失败", e);
                throw e;
            }
            
            // 6. 结果保存阶段
            ProfitCalculationResult finalResult;
            try {
                profitLogger.logProgress(taskId, "开始保存计算结果...");
                
                finalResult = saveResultsImproved(aggregationResult, taskId);
                
                // 任务成功完成
                profitLogger.logSuccess(taskId, "财务账单报告计算任务完成 - 共处理 %d 条记录", 
                    finalResult.getTotalRecords());
                
                profitReportTaskLogService.updateTaskStatus(taskId, ProfitReportTaskStatusEnum.SUCCESS, 
                    null, null, finalResult.getTotalRecords());
                taskManager.completeTask(taskId, finalResult);
                
                return CompletableFuture.completedFuture(finalResult);
                
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "结果保存阶段失败", e);
                throw e;
            }
            
        } catch (Exception e) {
            // 任务失败处理
            profitLogger.logStructuredException(taskId, "财务账单报告计算任务失败", e);
            
            profitReportTaskLogService.updateTaskStatus(taskId, ProfitReportTaskStatusEnum.FAILED, 
                e.getMessage(), null, 0);
            taskManager.failTask(taskId, e);
            
            // Java 8 兼容的失败Future创建方式
            CompletableFuture<ProfitCalculationResult> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
            
        } finally {
            // 清理MDC上下文
            MDC.remove(MDC_TASK_ID_KEY);
        }
    }
    
    /**
     * 构建计算范围描述
     */
    private String buildCalculationScope(ProfitCalculationRequestVO request) {
        // 只取第一个clientId
        String clientId = (request.getClientIds() != null && request.getClientIds().length > 0) 
                ? request.getClientIds()[0] : "未知";
        
        // 从financeDate数组获取日期
        String startDate = "未知";
        String endDate = "未知";
        if (request.getFinanceDate() != null && request.getFinanceDate().length >= 2) {
            startDate = request.getFinanceDate()[0];
            endDate = request.getFinanceDate()[1];
        }
        
        return String.format("店铺%s，%s至%s，%s维度", 
                clientId,
                startDate,
                endDate,
                request.getDimension() != null ? request.getDimension() : "client");
    }
    
    /**
     * 预估处理时间
     */
    private Integer estimateProcessingTime(ProfitCalculationRequestVO request) {
        // 基于日期范围和维度估算处理时间
        int baseTime = 1; // 默认1分钟
        
        try {
            if (request.getFinanceDate() != null && request.getFinanceDate().length >= 2) {
                LocalDate startDate = LocalDate.parse(request.getFinanceDate()[0]);
                LocalDate endDate = LocalDate.parse(request.getFinanceDate()[1]);
                long days = startDate.until(endDate).getDays() + 1;
                
                // 基础时间：每天1分钟
                baseTime = (int) days;
                
                // 根据维度调整时间
                if ("sku".equals(request.getDimension())) {
                    baseTime *= 2; // SKU维度需要更多时间
                }
            }
        } catch (Exception e) {
            log.warn("解析日期范围失败，使用默认处理时间: {}", e.getMessage());
        }
        
        return Math.max(baseTime, 1); // 最少1分钟
    }
    
    /**
     * 改进的结果保存方法，使用结构化日志
     */
    private ProfitCalculationResult saveResultsImproved(AggregationResult aggregationResult, String taskId) {
        try {
            // 批量保存聚合结果到数据库
            int recordsToSave = aggregationResult.getTotalRecords() != null ? aggregationResult.getTotalRecords() : 0;
            profitLogger.logProgress(taskId, "开始批量保存计算结果 - 待保存: %d 条", recordsToSave);
            
            int savedRecords = storageService.saveAggregationResult(aggregationResult, taskId);
            
            // 验证保存结果
            if (savedRecords != recordsToSave) {
                profitLogger.logStructuredWarning(taskId, "保存记录数不匹配: 预期 %d 条，实际保存 %d 条", 
                    recordsToSave, savedRecords);
            } else {
                profitLogger.logSuccess(taskId, "成功保存 %d 条计算结果", savedRecords);
            }
            
            // 构建最终结果
            Long processingDuration = aggregationResult.getStartTime() != null && aggregationResult.getEndTime() != null 
                ? java.time.Duration.between(aggregationResult.getStartTime(), aggregationResult.getEndTime()).getSeconds()
                : 0L;
                
            ProfitCalculationResult finalResult = ProfitCalculationResult.builder()
                    .taskId(taskId)
                    .totalRecords(savedRecords)
                    .duration(processingDuration)
                    .startTime(aggregationResult.getStartTime())
                    .endTime(aggregationResult.getEndTime())
                    .success(true)
                    .build();
            
            return finalResult;
            
        } catch (Exception e) {
            profitLogger.logStructuredException(taskId, "保存计算结果失败", e);
            throw new RuntimeException("结果保存失败", e);
        }
    }
    
    /**
     * 删除历史数据（参考V1版本的逻辑）
     */
    private void deleteHistoricalData(String taskId, ProfitCalculationRequestVO request) {
        if (request.getClientIds() == null || request.getClientIds().length == 0) {
            log.warn("客户端ID为空，跳过历史数据删除: taskId={}", taskId);
            profitReportTaskLogService.appendExecuteLog(taskId, "客户端ID为空，跳过历史数据删除");
            return;
        }
        
        if (request.getFinanceDate() == null || request.getFinanceDate().length < 2) {
            log.warn("日期范围不完整，跳过历史数据删除: taskId={}", taskId);
            profitReportTaskLogService.appendExecuteLog(taskId, "日期范围不完整，跳过历史数据删除");
            return;
        }
        
        try {
            // 解析日期范围
            LocalDate startDate = LocalDate.parse(request.getFinanceDate()[0]);
            LocalDate endDate = LocalDate.parse(request.getFinanceDate()[1]);
            
            log.info("删除历史数据: taskId={}, 客户端={}, 日期范围={} 至 {}", 
                    taskId, String.join(",", request.getClientIds()), startDate, endDate);
            
            profitReportTaskLogService.appendExecuteLog(taskId, 
                String.format("正在删除历史数据 - 客户端: %s, 日期范围: %s 至 %s", 
                    String.join(",", request.getClientIds()), startDate, endDate));
            
            // 逐个客户端删除历史数据，确保数据隔离
            for (String clientId : request.getClientIds()) {
                try {
                    int deletedCount = storageService.deleteResultsByClientIdAndDate(clientId, startDate, endDate);
                    log.info("客户端 {} 历史数据删除完成，删除记录数: {}", clientId, deletedCount);
                    profitReportTaskLogService.appendExecuteLog(taskId, 
                        String.format("客户端 %s 历史数据删除完成，删除记录数: %d", clientId, deletedCount));
                } catch (Exception e) {
                    String errorMsg = String.format("删除客户端 %s 历史数据失败: %s", clientId, e.getMessage());
                    log.error(errorMsg, e);
                    profitReportTaskLogService.appendErrorInfo(taskId, errorMsg);
                    // 继续处理其他客户端，不中断整个流程
                }
            }
            
            log.info("所有客户端历史数据删除完成: taskId={}", taskId);
            profitReportTaskLogService.appendExecuteLog(taskId, "所有客户端历史数据删除完成");
            
        } catch (Exception e) {
            String errorMsg = "删除历史数据失败: " + e.getMessage();
            log.error(errorMsg, e);
            profitReportTaskLogService.appendErrorInfo(taskId, errorMsg);
            profitReportTaskLogService.appendExecuteLog(taskId, errorMsg);
            throw new RuntimeException("删除历史数据失败", e);
        }
    }
} 