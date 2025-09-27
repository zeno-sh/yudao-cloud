package cn.iocoder.yudao.module.dm.service.profitreport.v2.data;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.AggregationConfig;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.AggregationResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据聚合器实现
 * 支持多维度数据聚合和格式转换
 * 
 * 根据数据层复用设计方案，直接处理ProfitReportDO格式
 *
 * @author Jax
 */
@Component
@Slf4j
public class DataAggregatorImpl implements DataAggregator {
    
    @Override
    public AggregationResult aggregate(CalculationResult calculationResult, ProfitCalculationRequestVO request, String taskId) {
        // 只取第一个clientId
        String clientId = (request.getClientIds() != null && request.getClientIds().length > 0) 
                ? request.getClientIds()[0] : null;
        
        log.info("开始执行数据聚合（修复记录翻倍问题）: taskId={}, 客户端ID={}", taskId, clientId);
        
        // 构建聚合配置（保留原有结构，实际只用作参数传递）
        AggregationConfig config = AggregationConfig.builder()
                .dimension("client_sku_date") // 修改为按客户端+SKU+日期维度聚合
                .timeType(request.getTimeType() != null ? request.getTimeType() : "day")
                .enableCache(true)
                .batchSize(1000)
                .enableParallel(true)
                .precision(4)
                .build();
        
        try {
            // **关键修复：只使用profitDetails，不合并costDetails和profitDetails**
            // costDetails是各计算器的原始成本记录，profitDetails是ProfitCalculator基于costDetails聚合的结果
            // 如果合并两者会导致数据重复计算，造成翻倍问题
            List<ProfitReportDO> reportsToAggregate;
            
            if (calculationResult.getProfitDetails() != null && !calculationResult.getProfitDetails().isEmpty()) {
                // 使用ProfitCalculator聚合后的利润明细（推荐方式）
                reportsToAggregate = calculationResult.getProfitDetails();
                log.info("使用利润明细进行聚合: taskId={}, 利润明细数={}", taskId, reportsToAggregate.size());
            } else {
                // 如果没有利润明细，使用成本明细（兜底方式）
                reportsToAggregate = calculationResult.getCostDetails();
                log.warn("利润明细为空，使用成本明细进行聚合: taskId={}, 成本明细数={}", taskId, reportsToAggregate.size());
            }
            
            // **方案一：直接聚合为ProfitReportDO列表，避免转换**
            List<ProfitReportDO> aggregatedReports = aggregateByPlatformSkuId(reportsToAggregate);
            
            // 构建聚合统计信息
            Map<String, Object> aggregationStats = buildAggregationStats(reportsToAggregate, aggregatedReports, config);
            
            AggregationResult result = AggregationResult.builder()
                    .taskId(calculationResult.getTaskId())
                    .aggregatedResults(aggregatedReports.stream().map(Object.class::cast).collect(Collectors.toList()))
                    .totalRecords(aggregatedReports.size())
                    .aggregationConfig(config)
                    .summaryData(aggregationStats)
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now())
                    .build();
            
            log.info("数据聚合完成（修复翻倍问题）: taskId={}, 原始记录数={}, 聚合后记录数={}, 成本明细数={}, 利润明细数={}", 
                    calculationResult.getTaskId(), 
                    reportsToAggregate.size(), 
                    aggregatedReports.size(),
                    calculationResult.getCostDetails() != null ? calculationResult.getCostDetails().size() : 0,
                    calculationResult.getProfitDetails() != null ? calculationResult.getProfitDetails().size() : 0);
            
            return result;
            
        } catch (Exception e) {
            log.error("数据聚合失败: taskId={}", calculationResult.getTaskId(), e);
            throw new RuntimeException("数据聚合失败", e);
        }
    }
    
    /**
     * 按 clientId + platformSkuId + financeDate 聚合，确保同一SKU在不同天的数据分别存储
     */
    private List<ProfitReportDO> aggregateByPlatformSkuId(List<ProfitReportDO> reports) {
        // 按 clientId + platformSkuId + financeDate 分组，确保不同店铺、不同天的数据分别存储
        Map<String, List<ProfitReportDO>> groupedByClientSkuAndDate = reports.stream()
                .collect(Collectors.groupingBy(report -> 
                        generateClientSkuDateKey(report.getClientId(), report.getPlatformSkuId(), report.getFinanceDate())));
        
        return groupedByClientSkuAndDate.values().stream()
                .map(this::aggregateProfitReportDOs)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 生成 clientId + platformSkuId + financeDate 的组合键
     */
    private String generateClientSkuDateKey(String clientId, String platformSkuId, LocalDate financeDate) {
        if (clientId == null || platformSkuId == null || financeDate == null) {
            log.warn("关键字段为空: clientId={}, platformSkuId={}, financeDate={}", clientId, platformSkuId, financeDate);
            return String.format("%s_%s_%s", 
                    clientId != null ? clientId : "unknown", 
                    platformSkuId != null ? platformSkuId : "unknown",
                    financeDate != null ? financeDate.toString() : "unknown");
        }
        return String.format("%s_%s_%s", clientId, platformSkuId, financeDate.toString());
    }
    
    /**
     * 聚合同一分组的ProfitReportDO列表，直接返回聚合后的ProfitReportDO
     */
    private ProfitReportDO aggregateProfitReportDOs(List<ProfitReportDO> reports) {
        if (reports.isEmpty()) {
            return null;
        }
        
        ProfitReportDO first = reports.get(0);
        
        // 创建聚合后的ProfitReportDO
        ProfitReportDO aggregated = ProfitReportDO.builder()
                .clientId(first.getClientId())
                .productId(first.getProductId()) // 保留productId
                .platformSkuId(first.getPlatformSkuId())
                .offerId(first.getOfferId())
                .financeDate(first.getFinanceDate())
                .platformCurrency(first.getPlatformCurrency())
                .fbsCurrency(first.getFbsCurrency())
                .purchaseCurrency(first.getPurchaseCurrency())
                .logisticsCurrency(first.getLogisticsCurrency())
                .customsCurrency(first.getCustomsCurrency())
                .build();
        
        // 设置必要的默认值
        aggregated.setDeleted(false);
        aggregated.setTenantId(cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.getTenantId());
        
        // 累加统计数据
        int totalOrders = reports.stream().mapToInt(r -> r.getOrders() != null ? r.getOrders() : 0).sum();
        int totalSalesVolume = reports.stream().mapToInt(r -> r.getSalesVolume() != null ? r.getSalesVolume() : 0).sum();
        int totalRefundOrders = reports.stream().mapToInt(r -> r.getRefundOrders() != null ? r.getRefundOrders() : 0).sum();
        
        aggregated.setOrders(totalOrders);
        aggregated.setSalesVolume(totalSalesVolume);
        aggregated.setRefundOrders(totalRefundOrders);
        
        // 累加金额数据
        aggregated.setSalesAmount(sumBigDecimal(reports, ProfitReportDO::getSalesAmount));
        aggregated.setSettleAmount(sumBigDecimal(reports, ProfitReportDO::getSettleAmount));

        // 累加成本数据
        aggregated.setCategoryCommissionCost(sumBigDecimal(reports, ProfitReportDO::getCategoryCommissionCost));
        aggregated.setReturnCommissionAmount(sumBigDecimal(reports, ProfitReportDO::getReturnCommissionAmount));
        aggregated.setCancelledAmount(sumBigDecimal(reports, ProfitReportDO::getCancelledAmount));
        aggregated.setReverseLogisticsCost(sumBigDecimal(reports, ProfitReportDO::getReverseLogisticsCost));
        aggregated.setOrderFeeCost(sumBigDecimal(reports, ProfitReportDO::getOrderFeeCost));
        aggregated.setLogisticsShippingCost(sumBigDecimal(reports, ProfitReportDO::getLogisticsShippingCost));
        aggregated.setLogisticsLastMileCost(sumBigDecimal(reports, ProfitReportDO::getLogisticsLastMileCost));
        aggregated.setLogisticsTransferCost(sumBigDecimal(reports, ProfitReportDO::getLogisticsTransferCost));
        aggregated.setLogisticsDropOff(sumBigDecimal(reports, ProfitReportDO::getLogisticsDropOff));
        aggregated.setOtherAgentServiceCost(sumBigDecimal(reports, ProfitReportDO::getOtherAgentServiceCost));
        aggregated.setRefundAmount(sumBigDecimal(reports, ProfitReportDO::getRefundAmount));
        aggregated.setPlatformServiceCost(sumBigDecimal(reports, ProfitReportDO::getPlatformServiceCost));
        
        // 仓储费用
        aggregated.setFboDeliverCost(sumBigDecimal(reports, ProfitReportDO::getFboDeliverCost));
        aggregated.setFboInspectionCost(sumBigDecimal(reports, ProfitReportDO::getFboInspectionCost));
        aggregated.setFbsCheckInCost(sumBigDecimal(reports, ProfitReportDO::getFbsCheckInCost));
        aggregated.setFbsOperatingCost(sumBigDecimal(reports, ProfitReportDO::getFbsOperatingCost));
        aggregated.setFbsOtherCost(sumBigDecimal(reports, ProfitReportDO::getFbsOtherCost));
        
        // 基础成本
        aggregated.setPurchaseCost(sumBigDecimal(reports, ProfitReportDO::getPurchaseCost));
        aggregated.setPurchaseShippingCost(sumBigDecimal(reports, ProfitReportDO::getPurchaseShippingCost));
        aggregated.setDeclaredValueCost(sumBigDecimal(reports, ProfitReportDO::getDeclaredValueCost));
        
        // 税务成本
        aggregated.setSalesVatCost(sumBigDecimal(reports, ProfitReportDO::getSalesVatCost));
        aggregated.setVatCost(sumBigDecimal(reports, ProfitReportDO::getVatCost));
        aggregated.setCustomsCost(sumBigDecimal(reports, ProfitReportDO::getCustomsCost));
        
        // 利润指标
        aggregated.setProfitAmount(sumBigDecimal(reports, ProfitReportDO::getProfitAmount));
        
        // 平台补偿金额
        aggregated.setCompensationAmount(sumBigDecimal(reports, ProfitReportDO::getCompensationAmount));
        
        return aggregated;
    }
    
    /**
     * 辅助方法：累加BigDecimal字段
     */
    private BigDecimal sumBigDecimal(List<ProfitReportDO> reports, 
                                   java.util.function.Function<ProfitReportDO, BigDecimal> getter) {
        return reports.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private Map<String, Object> buildAggregationStats(List<ProfitReportDO> originalReports, 
                                                     List<ProfitReportDO> aggregatedReports, 
                                                     AggregationConfig config) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("originalRecords", originalReports.size());
        stats.put("aggregatedRecords", aggregatedReports.size());
        stats.put("aggregationRatio", 
                originalReports.size() > 0 ? (double) aggregatedReports.size() / originalReports.size() : 0.0);
        stats.put("dimension", config.getDimension());
        stats.put("timeType", config.getTimeType());
        return stats;
    }
}