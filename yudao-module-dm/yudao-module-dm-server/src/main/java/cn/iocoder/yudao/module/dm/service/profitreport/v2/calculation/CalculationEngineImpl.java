package cn.iocoder.yudao.module.dm.service.profitreport.v2.calculation;

import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.framework.ParallelCalculationFramework;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationConfig;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.DataCollectionResult;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportTaskLogService;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.util.ProfitCalculationLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 计算引擎实现
 * 核心计算逻辑的统一入口，支持并行处理
 * 增强错误处理：详细记录计算过程中的各种异常和警告信息到任务日志
 *
 * @author Jax
 */
@Component
@Slf4j
public class CalculationEngineImpl implements CalculationEngine {
    
    @Resource
    private SignedOrderCostCalculator signedOrderCostCalculator;
    @Resource
    private ReturnOrderCostCalculator returnOrderCostCalculator;
    @Resource
    private OrderFeeCostCalculator orderFeeCostCalculator;
    @Resource
    private ProfitCalculator profitCalculator;
    @Resource
    private ParallelCalculationFramework parallelFramework;
    @Resource
    private ProfitReportTaskLogService profitReportTaskLogService;
    @Resource
    private ProfitCalculationLogger profitLogger;
    
    @Override
    public CalculationResult calculate(DataCollectionResult dataResult, String taskId) {
        LocalDateTime startTime = LocalDateTime.now();
        List<String> calculationExceptions = new ArrayList<>();
        
        try {
            // 验证输入数据
            validateInputData(dataResult, taskId);
            
            // 获取计算配置
            CalculationConfig config = createDefaultConfig();
            profitLogger.logProgress(taskId, "计算配置初始化完成 - 精度: %d 位小数, 并行处理: %s", 
                config.getPrecision(), config.getEnableParallelCalculation() ? "启用" : "禁用");
            
            List<ProfitReportDO> allCostDetails = new ArrayList<>();
            
            // 1. 使用并行框架执行各类成本计算
            profitLogger.logProgress(taskId, "开始并行执行成本计算...");
            
            List<CompletableFuture<List<ProfitReportDO>>> calculationTasks;
            try {
                calculationTasks = createCalculationTasks(dataResult, config, taskId);
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "创建并行计算任务失败", e);
                throw new RuntimeException("计算任务创建失败", e);
            }
            
            // 等待所有成本计算完成并收集结果
            List<List<ProfitReportDO>> calculationResults;
            try {
                calculationResults = parallelFramework.executeInParallel(calculationTasks);
                profitLogger.logProgress(taskId, "并行计算任务执行完成，开始收集结果...");
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "并行计算任务执行失败", e);
                throw new RuntimeException("并行计算执行失败", e);
            }
            
            // 合并所有计算结果
            try {
                for (List<ProfitReportDO> result : calculationResults) {
                    if (result != null) {
                        allCostDetails.addAll(result);
                    }
                }
                
                // 记录各类成本计算的详细结果
                recordCalculationResults(calculationResults, taskId);
                
                profitLogger.logSuccess(taskId, "成本计算结果汇总完成 - 总计生成: %d 条成本明细", 
                    allCostDetails.size());
                
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "成本计算结果汇总失败", e);
                throw new RuntimeException("计算结果汇总失败", e);
            }
            
            // 2. 利润指标计算
            List<ProfitReportDO> profitDetails;
            try {
                profitLogger.logProgress(taskId, "开始利润指标计算...");
                
                // 将成本明细数据添加到数据收集结果中，供利润计算器使用
                dataResult.setAllCostDetails(allCostDetails);
                
                profitDetails = profitCalculator.calculate(dataResult, config, taskId);
                
                profitLogger.logSuccess(taskId, "利润指标计算完成 - 生成: %d 条利润明细", 
                    profitDetails != null ? profitDetails.size() : 0);
                
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "利润指标计算失败", e);
                throw new RuntimeException("利润指标计算失败", e);
            }
            
            // 3. 构建计算统计信息
            Map<String, Integer> calculationStats;
            try {
                calculationStats = buildCalculationStats(dataResult, allCostDetails, profitDetails);
            } catch (Exception e) {
                profitLogger.logStructuredWarning(taskId, "构建计算统计信息失败: %s", e.getMessage());
                calculationExceptions.add("构建计算统计信息失败: " + e.getMessage());
                calculationStats = new HashMap<>();
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            
            CalculationResult result = CalculationResult.builder()
                    .taskId(taskId)
                    .startTime(startTime)
                    .endTime(endTime)
                    .costDetails(allCostDetails)
                    .profitDetails(profitDetails)
                    .calculationStats(calculationStats)
                    .exceptions(calculationExceptions)
                    .precision(config.getPrecision())
                    .build();
            
            // 记录最终计算结果统计
            profitLogger.logSuccess(taskId, "计算引擎完成 - 耗时: %d ms, 成本明细: %d 条, 利润明细: %d 条, 异常: %d 个", 
                durationMs, allCostDetails.size(), profitDetails.size(), calculationExceptions.size());
            
            // 如果有异常但不影响主流程，记录警告
            if (!calculationExceptions.isEmpty()) {
                profitLogger.logStructuredWarning(taskId, "计算过程中发现 %d 个异常，但不影响主要计算流程", 
                    calculationExceptions.size());
            }
            
            return result;
            
        } catch (Exception e) {
            profitLogger.logStructuredException(taskId, "计算引擎执行失败", e);
            
            // 构建失败结果
            CalculationResult errorResult = CalculationResult.builder()
                    .taskId(taskId)
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .costDetails(new ArrayList<>())
                    .profitDetails(new ArrayList<>())
                    .calculationStats(new HashMap<>())
                    .exceptions(Arrays.asList("计算引擎执行失败: " + e.getMessage()))
                    .precision(4)
                    .build();
            
            return errorResult;
        }
    }
    
    private void validateInputData(DataCollectionResult dataResult, String taskId) {
        profitLogger.logProgress(taskId, "开始验证输入数据完整性...");
        
        if (dataResult == null) {
            profitLogger.logStructuredError(taskId, "输入数据为空，无法进行计算");
            throw new IllegalArgumentException("输入数据为空");
        }
        
        // 验证订单数据
        if (dataResult.getOrderData() == null || 
            dataResult.getOrderData().getSignedOrders() == null ||
            dataResult.getOrderData().getSignedOrders().isEmpty()) {
            profitLogger.logStructuredWarning(taskId, "未找到签收订单数据，可能影响计算结果");
        }
        
        // 验证财务交易数据
        if (dataResult.getCostData() == null || 
            dataResult.getCostData().getFinanceTransactions() == null ||
            dataResult.getCostData().getFinanceTransactions().isEmpty()) {
            profitLogger.logStructuredWarning(taskId, "未找到财务交易数据，可能影响成本计算");
        }
        
        // 验证产品数据
        if (dataResult.getProductData() == null || 
            dataResult.getProductData().getProductPurchases() == null ||
            dataResult.getProductData().getProductPurchases().isEmpty()) {
            profitLogger.logStructuredWarning(taskId, "未找到产品数据，可能影响成本计算");
        }
        
        profitLogger.logSuccess(taskId, "输入数据验证通过");
    }
    
    /**
     * 记录各类成本计算的详细结果
     */
    private void recordCalculationResults(List<List<ProfitReportDO>> calculationResults, String taskId) {
        if (calculationResults == null || calculationResults.isEmpty()) {
            profitLogger.logStructuredWarning(taskId, "未收到任何计算结果");
            return;
        }
        
        int totalResults = 0;
        for (int i = 0; i < calculationResults.size(); i++) {
            List<ProfitReportDO> result = calculationResults.get(i);
            if (result != null) {
                totalResults += result.size();
                String calculationType = getCalculationTypeName(i);
                profitLogger.logImportantInfo(taskId, "%s 计算完成 - 生成: %d 条明细", 
                    calculationType, result.size());
            }
        }
        
        profitLogger.logImportantInfo(taskId, "所有成本计算任务完成 - 总计: %d 条明细", totalResults);
    }
    
    /**
     * 获取计算类型名称
     */
    private String getCalculationTypeName(int index) {
        switch (index) {
            case 0: return "签收订单成本";
            case 1: return "退货成本";
            case 2: return "收单费用";
            default: return "其他计算";
        }
    }
    
    /**
     * 创建计算任务（优化日志输出）
     */
    private List<CompletableFuture<List<ProfitReportDO>>> createCalculationTasks(
            DataCollectionResult dataResult, CalculationConfig config, String taskId) {
        
        List<CompletableFuture<List<ProfitReportDO>>> tasks = new ArrayList<>();
        
        // 签收订单成本计算任务
        if (signedOrderCostCalculator.supports(dataResult)) {
            CompletableFuture<List<ProfitReportDO>> signedTask = parallelFramework.supplyAsync(() -> {
                try {
                    List<ProfitReportDO> result = signedOrderCostCalculator.calculate(dataResult, config, taskId);
                    
                    if (result == null || result.isEmpty()) {
                        profitLogger.logStructuredWarning(taskId, "签收订单成本计算完成但未生成任何结果");
                        return new ArrayList<>();
                    }
                    
                    return result;
                    
                } catch (Exception e) {
                    profitLogger.logStructuredException(taskId, "签收订单成本计算失败", e);
                    throw new RuntimeException("签收订单成本计算失败: " + e.getMessage(), e);
                }
            });
            tasks.add(signedTask);
        } else {
            profitLogger.logStructuredWarning(taskId, "跳过签收订单成本计算 - 不支持当前数据");
        }
        
        // 退货成本计算任务
        if (returnOrderCostCalculator.supports(dataResult)) {
            CompletableFuture<List<ProfitReportDO>> returnTask = parallelFramework.supplyAsync(() -> {
                try {
                    List<ProfitReportDO> result = returnOrderCostCalculator.calculate(dataResult, config, taskId);
                    
                    if (result == null || result.isEmpty()) {
                        profitLogger.logStructuredWarning(taskId, "退货成本计算完成但未生成任何结果");
                        return new ArrayList<>();
                    }
                    
                    return result;
                    
                } catch (Exception e) {
                    profitLogger.logStructuredException(taskId, "退货成本计算失败", e);
                    throw new RuntimeException("退货成本计算失败: " + e.getMessage(), e);
                }
            });
            tasks.add(returnTask);
        } else {
            profitLogger.logStructuredWarning(taskId, "跳过退货成本计算 - 不支持当前数据");
        }
        
        // 收单费用计算任务
        if (orderFeeCostCalculator.supports(dataResult)) {
            CompletableFuture<List<ProfitReportDO>> orderFeeTask = parallelFramework.supplyAsync(() -> {
                try {
                    List<ProfitReportDO> result = orderFeeCostCalculator.calculate(dataResult, config, taskId);
                    
                    if (result == null || result.isEmpty()) {
                        profitLogger.logStructuredWarning(taskId, "收单费用计算完成但未生成任何结果");
                        return new ArrayList<>();
                    }
                    
                    return result;
                    
                } catch (Exception e) {
                    profitLogger.logStructuredException(taskId, "收单费用计算失败", e);
                    throw new RuntimeException("收单费用计算失败: " + e.getMessage(), e);
                }
            });
            tasks.add(orderFeeTask);
        } else {
            profitLogger.logStructuredWarning(taskId, "跳过收单费用计算 - 不支持当前数据");
        }
        
        return tasks;
    }
    
    /**
     * 构建计算统计信息
     */
    private Map<String, Integer> buildCalculationStats(DataCollectionResult dataResult, 
                                                      List<ProfitReportDO> costDetails, 
                                                      List<ProfitReportDO> profitDetails) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("processedOrders", dataResult.getOrderData().getTotalOrderCount());
        stats.put("processedItems", dataResult.getOrderData().getTotalItemCount());
        stats.put("processedTransactions", dataResult.getCostData().getTotalTransactionCount());
        stats.put("generatedCostDetails", costDetails.size());
        stats.put("generatedProfitDetails", profitDetails.size());
        
        // 按平台SKU统计
        Map<String, Long> costDetailsBySkuId = costDetails.stream()
                .collect(Collectors.groupingBy(
                    detail -> detail.getPlatformSkuId() != null ? detail.getPlatformSkuId() : "unknown",
                    Collectors.counting()));
        
        for (Map.Entry<String, Long> entry : costDetailsBySkuId.entrySet()) {
            stats.put("costDetails_" + entry.getKey(), entry.getValue().intValue());
        }
        
        return stats;
    }
    
    /**
     * 创建默认计算配置
     */
    private CalculationConfig createDefaultConfig() {
        return CalculationConfig.builder()
                .precision(4) // 4位小数精度
                .enableParallelCalculation(true) // 启用并行计算
                .enableCaching(true) // 启用缓存
                .maxRetryAttempts(3) // 最大重试次数
                .timeoutSeconds(300) // 超时时间：5分钟
                .batchSize(100) // 批处理大小
                .build();
    }
} 