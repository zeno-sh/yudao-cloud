package cn.iocoder.yudao.module.dm.service.profitreport.v2.data;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.DateRange;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitCalculationRequestVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.framework.ParallelCalculationFramework;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.*;
import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportTaskLogService;
import cn.iocoder.yudao.module.dm.service.transaction.OzonFinanceTransactionService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.util.ProfitCalculationLogger;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 数据收集器实现
 * 负责收集计算所需的所有基础数据，支持并行收集
 * 
 * 重要修复：统一收集财务交易数据，避免重复收集导致记录翻倍
 * 增强错误处理：详细记录采集过程中的各种异常和警告信息到任务日志
 *
 * @author Jax
 */
@Component
@Slf4j
public class DataCollectorImpl implements DataCollector {
    
    @Resource
    private OrderDataCollector orderDataCollector;
    @Resource
    private ProductDataCollector productDataCollector;
    @Resource
    private CostDataCollector costDataCollector;
    @Resource
    private ExchangeRateDataCollector exchangeRateDataCollector;
    @Resource
    private OzonFinanceTransactionService financeTransactionService;
    @Resource
    private ParallelCalculationFramework parallelFramework;
    @Resource
    private ProfitCalculationLogger profitLogger;
    
    @Override
    public DataCollectionResult collectData(ProfitCalculationRequestVO request, String taskId) {
        // 只取第一个clientId
        String clientId = (request.getClientIds() != null && request.getClientIds().length > 0) 
                ? request.getClientIds()[0] : null;
        
        profitLogger.logImportantInfo(taskId, "开始数据收集阶段 - 门店: %s", clientId);
        
        // 参数验证和错误记录
        if (clientId == null) {
            profitLogger.logStructuredError(taskId, "数据收集失败: 门店ID为空，请检查请求参数");
            throw new IllegalArgumentException("clientId不能为空");
        }
        
        if (request.getFinanceDate() == null || request.getFinanceDate().length < 2) {
            profitLogger.logStructuredError(taskId, "数据收集失败: 日期范围参数无效");
            throw new IllegalArgumentException("日期范围不能为空");
        }
        
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(request.getFinanceDate()[0]);
            endDate = LocalDate.parse(request.getFinanceDate()[1]);
        } catch (Exception e) {
            profitLogger.logStructuredException(taskId, "日期解析失败", e);
            throw new IllegalArgumentException("日期格式错误", e);
        }
        
        profitLogger.logProgress(taskId, "参数验证通过 - 门店: %s, 日期范围: %s 至 %s", 
            clientId, startDate, endDate);
        
        try {
            // **关键修复：在顶层统一收集财务交易数据，避免重复收集**
            profitLogger.logProgress(taskId, "开始统一收集财务交易数据，避免重复收集...");
                    
            List<OzonFinanceTransactionDO> financeTransactions;
            try {
                financeTransactions = financeTransactionService.getSigendTransactionList(
                        Collections.singletonList(clientId), 
                        new LocalDate[]{startDate, endDate});
                
                if (financeTransactions == null || financeTransactions.isEmpty()) {
                    profitLogger.logStructuredWarning(taskId, "未收集到财务交易数据 - 门店 %s 在 %s 至 %s 期间可能没有财务记录", 
                        clientId, startDate, endDate);
                    financeTransactions = Collections.emptyList();
                } else {
                    profitLogger.logSuccess(taskId, "成功收集财务交易数据: %d 条记录", financeTransactions.size());
                }
                
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "财务交易数据收集失败", e);
                throw new RuntimeException("财务交易数据收集失败", e);
            }
            
            // 使用并行框架收集各类数据，传递统一收集的财务交易数据
            profitLogger.logProgress(taskId, "开始并行收集订单数据、产品数据、成本数据、汇率数据...");
            
            List<CompletableFuture<Object>> dataCollectionTasks;
            try {
                dataCollectionTasks = createDataCollectionTasks(request, taskId, financeTransactions);
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "创建数据收集任务失败", e);
                throw new RuntimeException("数据收集任务创建失败", e);
            }
            
            // 等待所有数据收集完成
            List<Object> collectionResults;
            try {
                collectionResults = parallelFramework.executeInParallel(dataCollectionTasks);
                profitLogger.logProgress(taskId, "并行数据收集任务执行完成，开始验证收集结果...");
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "并行数据收集执行失败", e);
                throw new RuntimeException("并行数据收集失败", e);
            }
            
            // 按顺序提取结果并验证
            OrderData orderData = null;
            ProductData productData = null;
            CostData costData = null;
            ExchangeRateData exchangeRateData = null;
            
            try {
                orderData = (OrderData) collectionResults.get(0);
                productData = (ProductData) collectionResults.get(1);
                costData = (CostData) collectionResults.get(2);
                exchangeRateData = (ExchangeRateData) collectionResults.get(3);
                
                // 验证各项数据的完整性和合理性
                validateCollectedData(orderData, productData, costData, exchangeRateData, taskId);
                
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "数据收集结果提取或验证失败", e);
                throw new RuntimeException("数据结果处理失败", e);
            }
            
            // 构建DateRange对象
            DateRange dateRange = null;
            if (request.getFinanceDate() != null && request.getFinanceDate().length >= 2) {
                dateRange = new DateRange();
                dateRange.setStartDate(LocalDate.parse(request.getFinanceDate()[0]));
                dateRange.setEndDate(LocalDate.parse(request.getFinanceDate()[1]));
            }
            
            DataCollectionResult result = DataCollectionResult.builder()
                .orderData(orderData)
                .productData(productData)
                .costData(costData)
                .exchangeRateData(exchangeRateData)
                .clientId(clientId)
                .dateRange(dateRange)
                .taskId(taskId)
                .collectionTime(java.time.LocalDateTime.now())
                .totalDataSets(4)
                .build();
            
            // 记录最终收集结果统计
            profitLogger.logSuccess(taskId, "数据收集完成 - 订单数据: %d 条, 财务交易数据: %d 条, 产品数据: %d 条", 
                orderData.getTotalOrderCount(),
                costData.getTotalTransactionCount(),
                productData.getTotalProductCount());
            
            return result;
            
        } catch (Exception e) {
            profitLogger.logStructuredException(taskId, "数据收集过程失败", e);
            throw new RuntimeException("数据收集失败", e);
        }
    }
    
    /**
     * 验证收集到的数据（使用结构化日志）
     */
    private void validateCollectedData(OrderData orderData, ProductData productData, 
                                     CostData costData, ExchangeRateData exchangeRateData, String taskId) {
        
        // 验证订单数据
        if (orderData == null) {
            profitLogger.logStructuredError(taskId, "订单数据收集失败，返回null");
        } else if (orderData.getTotalOrderCount() == 0) {
            profitLogger.logStructuredWarning(taskId, "未收集到任何订单数据，请检查日期范围和门店配置");
        } else if (orderData.getTotalItemCount() == 0) {
            profitLogger.logStructuredWarning(taskId, "收集到 %d 个订单但没有订单明细，数据可能不完整", 
                orderData.getTotalOrderCount());
        } else {
            profitLogger.logImportantInfo(taskId, "订单数据验证通过 - 订单: %d 条, 明细: %d 条", 
                orderData.getTotalOrderCount(), orderData.getTotalItemCount());
        }
        
        // 验证产品数据
        if (productData == null) {
            profitLogger.logStructuredError(taskId, "产品数据收集失败，返回null");
        } else if (productData.getTotalProductCount() == 0) {
            profitLogger.logProductNotMapped(taskId, "门店产品配置");
        } else {
            profitLogger.logImportantInfo(taskId, "产品数据验证通过 - 产品: %d 条", 
                productData.getTotalProductCount());
            
            // 检查产品映射关系
            if (productData.getPlatformSkuIdMapping() == null || productData.getPlatformSkuIdMapping().isEmpty()) {
                profitLogger.logStructuredWarning(taskId, "平台SKU映射关系为空，可能影响成本计算");
            }
        }
        
        // 验证成本数据
        if (costData == null) {
            profitLogger.logStructuredError(taskId, "成本数据收集失败，返回null");
        } else if (costData.getTotalTransactionCount() == 0) {
            profitLogger.logStructuredWarning(taskId, "未收集到任何成本数据，请检查财务交易记录");
        } else {
            profitLogger.logImportantInfo(taskId, "成本数据验证通过 - 交易: %d 条", 
                costData.getTotalTransactionCount());
        }
        
        // 验证汇率数据（重要警告）
        if (exchangeRateData == null) {
            profitLogger.logExchangeRateMissing(taskId, "所有币种", "指定日期范围");
        } else if (exchangeRateData.getTotalRateCount() == 0) {
            profitLogger.logExchangeRateMissing(taskId, "多币种", "指定日期范围");
        } else {
            profitLogger.logImportantInfo(taskId, "汇率数据验证通过 - 汇率: %d 条", 
                exchangeRateData.getTotalRateCount());
        }
    }
    
    /**
     * 创建数据收集任务列表（优化日志输出）
     */
    private List<CompletableFuture<Object>> createDataCollectionTasks(
            ProfitCalculationRequestVO request, 
            String taskId, 
            List<OzonFinanceTransactionDO> financeTransactions) {
        
        // 创建并行数据收集任务，传递统一收集的财务交易数据
        CompletableFuture<Object> orderTask = parallelFramework.supplyAsync(() -> {
            try {
                OrderData result = orderDataCollector.collectWithFinanceTransactions(request, taskId, financeTransactions);
                profitLogger.logImportantInfo(taskId, "订单数据收集完成 - 订单: %d 条, 明细: %d 条", 
                    result.getTotalOrderCount(), result.getTotalItemCount());
                return result;
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "订单数据收集任务失败", e);
                throw new RuntimeException("订单数据收集任务失败: " + e.getMessage(), e);
            }
        });
        
        CompletableFuture<Object> productTask = parallelFramework.supplyAsync(() -> {
            try {
                ProductData result = productDataCollector.collect(request, taskId);
                profitLogger.logImportantInfo(taskId, "产品数据收集完成 - 产品: %d 条", 
                    result.getTotalProductCount());
                return result;
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "产品数据收集任务失败", e);
                throw new RuntimeException("产品数据收集任务失败: " + e.getMessage(), e);
            }
        });
        
        CompletableFuture<Object> costTask = parallelFramework.supplyAsync(() -> {
            try {
                CostData result = costDataCollector.collectWithFinanceTransactions(request, taskId, financeTransactions);
                profitLogger.logImportantInfo(taskId, "成本数据收集完成 - 交易: %d 条", 
                    result.getTotalTransactionCount());
                return result;
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "成本数据收集任务失败", e);
                throw new RuntimeException("成本数据收集任务失败: " + e.getMessage(), e);
            }
        });
        
        CompletableFuture<Object> exchangeRateTask = parallelFramework.supplyAsync(() -> {
            try {
                ExchangeRateData result = exchangeRateDataCollector.collect(request, taskId);
                if (result.getTotalRateCount() == 0) {
                    profitLogger.logExchangeRateMissing(taskId, "多币种", "指定日期范围");
                } else {
                    profitLogger.logImportantInfo(taskId, "汇率数据收集完成 - 汇率: %d 条", 
                        result.getTotalRateCount());
                }
                return result;
            } catch (Exception e) {
                profitLogger.logStructuredException(taskId, "汇率数据收集任务失败", e);
                throw new RuntimeException("汇率数据收集任务失败: " + e.getMessage(), e);
            }
        });
        
        return Lists.newArrayList(orderTask, productTask, costTask, exchangeRateTask);
    }
} 