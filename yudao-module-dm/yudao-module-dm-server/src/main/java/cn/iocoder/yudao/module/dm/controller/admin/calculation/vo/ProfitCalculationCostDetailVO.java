package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "利润计算成本明细 VO")
@Data
public class ProfitCalculationCostDetailVO {
    
    // 计算中间结果
    @Schema(description = "立方米")
    private BigDecimal volumeM3;
    
    @Schema(description = "计算重量")
    private BigDecimal calculateWeight;
    
    @Schema(description = "计算体积")
    private BigDecimal calculateVolume;
    
    @Schema(description = "实际体积")
    private BigDecimal actualVolume;
    
    @Schema(description = "实际重量")
    private BigDecimal actualWeight;
    
    // 各项费用明细
    @Schema(description = "采购单价（CNY，不含税）")
    private BigDecimal purchaseUnit;
    
    @Schema(description = "采购成本（目标币种，根据汇率计算）")
    private BigDecimal purchaseCost;
    
    @Schema(description = "国内运费")
    private BigDecimal localTransportCost;
    
    @Schema(description = "货代费用")
    private BigDecimal freightForwarderCost;
    
    @Schema(description = "关税费用")
    private BigDecimal tariffCost;
    
    @Schema(description = "VAT费用")
    private BigDecimal vatCost;
    
    @Schema(description = "头程运费")
    private BigDecimal firstMileFreightCost;
    
    @Schema(description = "配送费")
    private BigDecimal deliveryCost;
    
    @Schema(description = "仓储费")
    private BigDecimal storageCost;
    
    @Schema(description = "类目佣金费用")
    private BigDecimal categoryCommissionCost;
    
    @Schema(description = "数字服务费用")
    private BigDecimal digitalServiceCost;
    
    @Schema(description = "广告费用")
    private BigDecimal adCost;
    
    @Schema(description = "退换货费用")
    private BigDecimal returnCost;
    
}