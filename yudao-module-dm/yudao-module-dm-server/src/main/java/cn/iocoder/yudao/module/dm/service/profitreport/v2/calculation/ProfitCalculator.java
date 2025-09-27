package cn.iocoder.yudao.module.dm.service.profitreport.v2.calculation;

import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationConfig;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.DataCollectionResult;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.util.CalculationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 利润计算器
 * 基于其他计算器产生的ProfitReportDO计算利润指标
 * 
 * 根据数据层复用设计方案，直接操作ProfitReportDO格式
 *
 * @author Jax
 */
@Component
@Slf4j
public class ProfitCalculator implements CostCalculator {
    
    @Override
    public CostCalculatorType getType() {
        return CostCalculatorType.PROFIT;
    }
    
    @Override
    public boolean supports(DataCollectionResult dataResult) {
        // 利润计算器总是支持，因为它处理的是其他计算器的结果
        return true;
    }
    
    @Override
    public List<ProfitReportDO> calculate(
            DataCollectionResult dataResult, 
            CalculationConfig config, 
            String taskId) {
        
        log.info("开始计算利润指标: taskId={}", taskId);
        
        List<ProfitReportDO> results = new ArrayList<>();
        
        try {
            // 获取所有成本明细（来自其他计算器的结果）
            List<ProfitReportDO> allCostDetails = dataResult.getAllCostDetails();
            
            if (allCostDetails == null || allCostDetails.isEmpty()) {
                log.warn("没有成本明细数据用于利润计算: taskId={}", taskId);
                return results;
            }
            
            // 按产品维度聚合成本明细
            Map<String, List<ProfitReportDO>> groupedByProduct = allCostDetails.stream()
                    .collect(Collectors.groupingBy(detail -> 
                            generateProductKey(detail.getClientId(), detail.getPlatformSkuId(), detail.getFinanceDate())));
            
            // 计算每个产品的利润指标
            for (Map.Entry<String, List<ProfitReportDO>> entry : groupedByProduct.entrySet()) {
                String productKey = entry.getKey();
                List<ProfitReportDO> productCostDetails = entry.getValue();
                
                // 合并同一产品的所有成本明细
                ProfitReportDO mergedProfitReport = mergeProfitReportDOs(productCostDetails, config);
                
                // 计算利润指标
//                calculateProfitMetrics(mergedProfitReport, config);
                results.add(mergedProfitReport);
                
            }
            
            log.info("利润指标计算完成: taskId={}, 处理产品数={}, 生成利润明细数={}", 
                    taskId, groupedByProduct.size(), results.size());
            
        } catch (Exception e) {
            log.error("利润指标计算失败: taskId={}", taskId, e);
            throw new RuntimeException("利润指标计算失败", e);
        }
        
        return results;
    }
    
    /**
     * 生成产品唯一标识
     */
    private String generateProductKey(String clientId, String platformSkuId, java.time.LocalDate financeDate) {
        return String.format("%s_%s_%s", clientId, platformSkuId, financeDate.toString());
    }
    
    /**
     * 合并同一产品的多个ProfitReportDO
     */
    private ProfitReportDO mergeProfitReportDOs(List<ProfitReportDO> profitReports, CalculationConfig config) {
        if (profitReports.isEmpty()) {
            throw new IllegalArgumentException("利润报告列表不能为空");
        }
        
        if (profitReports.size() == 1) {
            return profitReports.get(0);
        }
        
        // 使用第一个报告作为基础
        ProfitReportDO first = profitReports.get(0);
        ProfitReportDO merged = ProfitReportDO.builder()
                .clientId(first.getClientId())
                .productId(first.getProductId())
                .platformSkuId(first.getPlatformSkuId())
                .offerId(first.getOfferId())
                .financeDate(first.getFinanceDate())
                .platformCurrency(first.getPlatformCurrency())
                .fbsCurrency(first.getFbsCurrency())
                .purchaseCurrency(first.getPurchaseCurrency())
                .logisticsCurrency(first.getLogisticsCurrency())
                .customsCurrency(first.getCustomsCurrency())
                .orders(0)
                .salesVolume(0)
                .build();
        
        // 设置必要的默认值，避免数据库约束错误
        merged.setDeleted(false);
        merged.setTenantId(cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.getTenantId());
        
        // 合并所有成本明细
        for (ProfitReportDO report : profitReports) {
            // 累加订单数和销量
            merged.setOrders(merged.getOrders() + (report.getOrders() != null ? report.getOrders() : 0));
            merged.setSalesVolume(merged.getSalesVolume() + (report.getSalesVolume() != null ? report.getSalesVolume() : 0));
            
            // 累加各类成本
            merged.setSalesAmount(addBigDecimal(merged.getSalesAmount(), report.getSalesAmount()));
            merged.setSettleAmount(addBigDecimal(merged.getSettleAmount(), report.getSettleAmount()));
            merged.setCategoryCommissionCost(addBigDecimal(merged.getCategoryCommissionCost(), report.getCategoryCommissionCost()));
            merged.setReturnCommissionAmount(addBigDecimal(merged.getReturnCommissionAmount(), report.getReturnCommissionAmount()));
            merged.setCancelledAmount(addBigDecimal(merged.getCancelledAmount(), report.getCancelledAmount()));
            merged.setReverseLogisticsCost(addBigDecimal(merged.getReverseLogisticsCost(), report.getReverseLogisticsCost()));
            merged.setOrderFeeCost(addBigDecimal(merged.getOrderFeeCost(), report.getOrderFeeCost()));
            merged.setLogisticsShippingCost(addBigDecimal(merged.getLogisticsShippingCost(), report.getLogisticsShippingCost()));
            merged.setLogisticsLastMileCost(addBigDecimal(merged.getLogisticsLastMileCost(), report.getLogisticsLastMileCost()));
            merged.setLogisticsTransferCost(addBigDecimal(merged.getLogisticsTransferCost(), report.getLogisticsTransferCost()));
            merged.setLogisticsDropOff(addBigDecimal(merged.getLogisticsDropOff(), report.getLogisticsDropOff()));
            merged.setOtherAgentServiceCost(addBigDecimal(merged.getOtherAgentServiceCost(), report.getOtherAgentServiceCost()));
            merged.setRefundOrders((merged.getRefundOrders() != null ? merged.getRefundOrders() : 0) + 
                                  (report.getRefundOrders() != null ? report.getRefundOrders() : 0));
            merged.setRefundAmount(addBigDecimal(merged.getRefundAmount(), report.getRefundAmount()));
            merged.setPlatformServiceCost(addBigDecimal(merged.getPlatformServiceCost(), report.getPlatformServiceCost()));
            merged.setFboDeliverCost(addBigDecimal(merged.getFboDeliverCost(), report.getFboDeliverCost()));
            merged.setFboInspectionCost(addBigDecimal(merged.getFboInspectionCost(), report.getFboInspectionCost()));
            merged.setFbsCheckInCost(addBigDecimal(merged.getFbsCheckInCost(), report.getFbsCheckInCost()));
            merged.setFbsOperatingCost(addBigDecimal(merged.getFbsOperatingCost(), report.getFbsOperatingCost()));
            merged.setFbsOtherCost(addBigDecimal(merged.getFbsOtherCost(), report.getFbsOtherCost()));
            merged.setSalesVatCost(addBigDecimal(merged.getSalesVatCost(), report.getSalesVatCost()));
            merged.setVatCost(addBigDecimal(merged.getVatCost(), report.getVatCost()));
            merged.setCustomsCost(addBigDecimal(merged.getCustomsCost(), report.getCustomsCost()));
            merged.setPurchaseCost(addBigDecimal(merged.getPurchaseCost(), report.getPurchaseCost()));
            merged.setPurchaseShippingCost(addBigDecimal(merged.getPurchaseShippingCost(), report.getPurchaseShippingCost()));
            merged.setDeclaredValueCost(addBigDecimal(merged.getDeclaredValueCost(), report.getDeclaredValueCost()));
            merged.setCompensationAmount(addBigDecimal(merged.getCompensationAmount(), report.getCompensationAmount()));
        }
        
        return merged;
    }
    
    /**
     * 计算利润指标
     */
    private void calculateProfitMetrics(ProfitReportDO profitReport, CalculationConfig config) {

        // 1. 获取销售金额
        BigDecimal salesAmount = profitReport.getSalesAmount() != null ?
                profitReport.getSalesAmount() : BigDecimal.ZERO;

        // 2. 计算结算金额 = 销售金额 + 退还佣金 - 取消金额 - 类目佣金 - 送货费 - 收单 - 逆向物流 - 赔偿
        BigDecimal settleAmount = salesAmount
                .add(getNotNullValue(profitReport.getReturnCommissionAmount()))  // 退还佣金
                .subtract(getAbsoluteValue(profitReport.getCancelledAmount()))    // 取消金额
                .subtract(getAbsoluteValue(profitReport.getCategoryCommissionCost())) // 类目佣金
                .subtract(getAbsoluteValue(profitReport.getLogisticsLastMileCost()))  // 送货费
                .subtract(getAbsoluteValue(profitReport.getLogisticsTransferCost()))  // 送货费
                .subtract(getAbsoluteValue(profitReport.getLogisticsDropOff()))       // 送货费
                .subtract(getAbsoluteValue(profitReport.getOtherAgentServiceCost()))  // 其他代理服务费
                .add(getNotNullValue(profitReport.getOrderFeeCost()))            // 收单费用(有正有负所以用加法)
                .subtract(getAbsoluteValue(profitReport.getReverseLogisticsCost()))   // 逆向物流
                .subtract(getAbsoluteValue(profitReport.getRefundAmount()))           // 赔偿
                .add(getNotNullValue(profitReport.getCompensationAmount()));          // 平台补偿金额
        profitReport.setSettleAmount(settleAmount);

        // 4. 计算总成本（所有负数成本的绝对值之和）
        BigDecimal totalCost = BigDecimal.ZERO;

        // 平台成本
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getCategoryCommissionCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getLogisticsLastMileCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getLogisticsTransferCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getLogisticsDropOff()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getOtherAgentServiceCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getReverseLogisticsCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getOrderFeeCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getPlatformServiceCost()));

        // 仓储成本
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getFboDeliverCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getFboInspectionCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getFbsCheckInCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getFbsOperatingCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getFbsOtherCost()));

        // 基础成本
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getPurchaseCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getPurchaseShippingCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getLogisticsShippingCost()));

        // 税务成本
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getSalesVatCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getVatCost()));
        totalCost = totalCost.add(getAbsoluteValue(profitReport.getCustomsCost()));

        // 5. 计算利润 = 结算金额 - 总成本
        BigDecimal profit = CalculationUtils.subtract(settleAmount, totalCost, config);
        profitReport.setProfitAmount(profit);

        log.debug("利润计算完成: platformSkuId={}, 销售金额={}, 结算金额={}, 总成本={}, 利润={}",
                profitReport.getPlatformSkuId(), salesAmount, settleAmount, totalCost, profit);
    }

    /**
     * 安全的BigDecimal加法
     */
    private BigDecimal addBigDecimal(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        return a.add(b);
    }
    
    /**
     * 获取绝对值，null返回0
     */
    private BigDecimal getAbsoluteValue(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO;
        return value.abs();
    }
    
    /**
     * 获取非null值，null返回0
     */
    private BigDecimal getNotNullValue(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO;
        return value;
    }
}