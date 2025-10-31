package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 利润报告结果（新版）
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 利润报告结果（V2版本）")
@Data
@Builder
public class ProfitReportResultVO {
    
    // ========== 基础信息 ==========
    @Schema(description = "门店ID", example = "client1")
    private String clientId;
    
    @Schema(description = "本地产品ID", example = "123456")
    private Long productId;
    
    @Schema(description = "平台SKU ID", example = "SKU123")
    private String platformSkuId;
    
    @Schema(description = "offer ID", example = "OFFER123")
    private String offerId;
    
    @Schema(description = "财务日期", example = "2024-01-01")
    private LocalDate financeDate;
    
    // ========== 销售指标 ==========
    @Schema(description = "订单数量", example = "10")
    private Integer orders;
    
    @Schema(description = "销售数量", example = "100")
    private Integer salesVolume;
    
    @Schema(description = "退货订单数", example = "2")
    private Integer refundOrders;
    
    @Schema(description = "销售金额", example = "1000.00")
    private MonetaryAmount salesAmount;
    
    @Schema(description = "结算金额", example = "950.00")
    private MonetaryAmount settleAmount;
    
    @Schema(description = "退货结算金额", example = "50.00")
    private MonetaryAmount refundSettleAmount;
    
    // ========== 成本明细 ==========
    @Schema(description = "成本明细")
    private CostBreakdown costBreakdown;
    
    // ========== 利润指标 ==========
    @Schema(description = "利润指标")
    private ProfitMetrics profitMetrics;
    
    // ========== 计算元数据 ==========
    @Schema(description = "计算元数据")
    private CalculationMetadata metadata;
} 