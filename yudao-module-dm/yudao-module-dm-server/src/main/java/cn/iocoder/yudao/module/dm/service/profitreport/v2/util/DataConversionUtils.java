package cn.iocoder.yudao.module.dm.service.profitreport.v2.util;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据转换工具类
 * 根据数据层复用设计方案，只处理ProfitReportDO相关的转换
 *
 * @author Jax
 */
@Slf4j
public class DataConversionUtils {
    
    // 默认精度
    private static final int DEFAULT_SCALE = 4;
    private static final int DISPLAY_SCALE = 2;
    
    /**
     * ProfitReportDO转换为ProfitReportResultVO
     */
    public static ProfitReportResultVO convertToResultVO(ProfitReportDO resultDO) {
        if (resultDO == null) {
            return null;
        }
        
        // 基础信息
        ProfitReportResultVO resultVO = ProfitReportResultVO.builder()
                .clientId(resultDO.getClientId())
                .platformSkuId(resultDO.getPlatformSkuId())
                .offerId(resultDO.getOfferId())
                .financeDate(resultDO.getFinanceDate())
                .orders(resultDO.getOrders())
                .salesVolume(resultDO.getSalesVolume())
                .build();
        
        // 销售金额
        if (resultDO.getSalesAmount() != null) {
            resultVO.setSalesAmount(MonetaryAmount.builder()
                    .amount(resultDO.getSalesAmount().setScale(DISPLAY_SCALE, RoundingMode.HALF_UP))
                    .currencyCode(convertCurrencyIntegerToString(resultDO.getPlatformCurrency()))
                    .build());
        }
        
        // 成本明细
        CostBreakdown costBreakdown = CostBreakdown.builder()
                .commission(convertToMonetaryAmount(resultDO.getCategoryCommissionCost(), resultDO.getPlatformCurrency()))
                .lastMileDelivery(convertToMonetaryAmount(resultDO.getLogisticsLastMileCost(), resultDO.getPlatformCurrency()))
                .transferCost(convertToMonetaryAmount(resultDO.getLogisticsTransferCost(), resultDO.getPlatformCurrency()))
                .pickupCost(convertToMonetaryAmount(resultDO.getLogisticsDropOff(), resultDO.getPlatformCurrency()))
                .orderFee(convertToMonetaryAmount(resultDO.getOrderFeeCost(), resultDO.getPlatformCurrency()))
                .otherAgentService(convertToMonetaryAmount(resultDO.getOtherAgentServiceCost(), resultDO.getPlatformCurrency()))
                .purchaseCost(convertToMonetaryAmount(resultDO.getPurchaseCost(), resultDO.getPurchaseCurrency()))
                .purchaseShipping(convertToMonetaryAmount(resultDO.getPurchaseShippingCost(), resultDO.getPurchaseCurrency()))
                .firstMileShipping(convertToMonetaryAmount(resultDO.getLogisticsShippingCost(), resultDO.getLogisticsCurrency()))
                .customsDuty(convertToMonetaryAmount(resultDO.getCustomsCost(), resultDO.getCustomsCurrency()))
                .importVat(convertToMonetaryAmount(resultDO.getVatCost(), resultDO.getCustomsCurrency()))
                .salesVat(convertToMonetaryAmount(resultDO.getSalesVatCost(), resultDO.getPlatformCurrency()))
                .fbsStorageFee(convertToMonetaryAmount(addBigDecimal(resultDO.getFbsCheckInCost(), resultDO.getFbsOperatingCost(), resultDO.getFbsOtherCost()), resultDO.getFbsCurrency()))
                .fboDeliveryFee(convertToMonetaryAmount(resultDO.getFboDeliverCost(), resultDO.getFbsCurrency()))
                .fboInspectionFee(convertToMonetaryAmount(resultDO.getFboInspectionCost(), resultDO.getFbsCurrency()))
                .compensationAmount(convertToMonetaryAmount(resultDO.getCompensationAmount(), resultDO.getPlatformCurrency()))
                .build();
        resultVO.setCostBreakdown(costBreakdown);
        
        // 利润指标
        // 计算利润率
        BigDecimal grossProfitRate = calculateGrossProfitRate(resultDO.getProfitAmount(), resultDO.getSalesAmount());
        
        ProfitMetrics profitMetrics = ProfitMetrics.builder()
                .grossProfit(convertToMonetaryAmount(resultDO.getProfitAmount(), resultDO.getPlatformCurrency()))
                .grossProfitRate(grossProfitRate)
                .build();
        resultVO.setProfitMetrics(profitMetrics);
        
        // 计算元数据
        CalculationMetadata metadata = CalculationMetadata.builder()
                .calculationType("profit_report")
                .calculationTime(LocalDateTime.now())
                .dataSource("OZON_API")
                .precision(DEFAULT_SCALE)
                .build();
        resultVO.setMetadata(metadata);
        
        return resultVO;
    }
    
    /**
     * 批量转换ProfitReportDO为ProfitReportResultVO
     */
    public static List<ProfitReportResultVO> convertToResultVOList(List<ProfitReportDO> resultDOs) {
        if (resultDOs == null || resultDOs.isEmpty()) {
            return Lists.newArrayListWithCapacity(0);
        }
        
        return resultDOs.stream()
                .map(DataConversionUtils::convertToResultVO)
                .collect(Collectors.toList());
    }
    
    /**
     * MonetaryAmount转换为BigDecimal
     */
    private static BigDecimal convertToDecimal(MonetaryAmount amount) {
        if (amount == null || amount.getAmount() == null) {
            return BigDecimal.ZERO;
        }
        return amount.getAmount().setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }
    
    /**
     * BigDecimal转换为MonetaryAmount
     */
    private static MonetaryAmount convertToMonetaryAmount(BigDecimal amount, Integer currencyId) {
        if (amount == null) {
            return MonetaryAmount.builder()
                    .amount(BigDecimal.ZERO)
                    .currencyCode(convertCurrencyIntegerToString(currencyId))
                    .build();
        }
        
        return MonetaryAmount.builder()
                .amount(amount.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP))
                .currencyCode(convertCurrencyIntegerToString(currencyId))
                .build();
    }

    /**
     * 币种整数转换为字符串（根据字典表）
     */
    private static String convertCurrencyIntegerToString(Integer currencyId) {
        if (currencyId == null) {
            return "CNY";
        }
        // 这里应该根据实际的字典表进行转换
        switch (currencyId) {
            case 1:
                return "CNY";
            case 2:
                return "USD";
            case 3:
                return "RUB";
            default:
                return "CNY";
        }
    }

    /**
     * 计算毛利率
     */
    private static BigDecimal calculateGrossProfitRate(BigDecimal profitAmount, BigDecimal salesAmount) {
        if (profitAmount == null || salesAmount == null || salesAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return profitAmount.divide(salesAmount, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * BigDecimal转换为百分比
     */
    private static BigDecimal convertToPercentage(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算持续时间的友好显示
     */
    public static String formatDuration(Duration duration) {
        if (duration == null) {
            return "0秒";
        }
        
        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d小时%d分钟%d秒", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }
    
    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1fKB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1fMB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1fGB", bytes / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 计算处理速度
     */
    public static String calculateProcessingSpeed(int recordCount, Duration duration) {
        if (duration == null || duration.isZero()) {
            return "0条/秒";
        }
        
        double secondsTotal = duration.toMillis() / 1000.0;
        double speed = recordCount / secondsTotal;
        
        if (speed >= 1000) {
            return String.format("%.1fK条/秒", speed / 1000);
        } else {
            return String.format("%.1f条/秒", speed);
        }
    }
    
    /**
     * 构建错误描述
     */
    public static String buildErrorDescription(Exception e) {
        if (e == null) {
            return "未知错误";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(": ");
        
        if (e.getMessage() != null) {
            sb.append(e.getMessage());
        } else {
            sb.append("无详细错误信息");
        }
        
        return sb.toString();
    }
    
    /**
     * 合并多个BigDecimal值
     */
    private static BigDecimal addBigDecimal(BigDecimal... values) {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (value != null) {
                result = result.add(value);
            }
        }
        return result;
    }
}