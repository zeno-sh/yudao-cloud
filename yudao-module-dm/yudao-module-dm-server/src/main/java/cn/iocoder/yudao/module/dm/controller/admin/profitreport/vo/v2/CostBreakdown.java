package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

/**
 * 成本明细
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 成本明细")
@Data
@Builder
public class CostBreakdown {
    
    // ========== 平台费用 ==========
    @Schema(description = "类目佣金", example = "-50.00")
    private MonetaryAmount commission;
    
    @Schema(description = "最后一公里费用", example = "-30.00")
    private MonetaryAmount lastMileDelivery;
    
    @Schema(description = "转运费用", example = "-20.00")
    private MonetaryAmount transferCost;
    
    @Schema(description = "取件费用", example = "-10.00")
    private MonetaryAmount pickupCost;
    
    @Schema(description = "收单费用", example = "-5.00")
    private MonetaryAmount orderFee;
    
    @Schema(description = "其他代理服务费", example = "-8.00")
    private MonetaryAmount otherAgentService;
    
    // ========== 退货相关 ==========
    @Schema(description = "逆向物流费用", example = "-15.00")
    private MonetaryAmount reverseLogistics;
    
    @Schema(description = "赔偿金额", example = "-20.00")
    private MonetaryAmount refundAmount;
    
    @Schema(description = "退还佣金", example = "10.00")
    private MonetaryAmount returnCommission;
    
    @Schema(description = "取消金额", example = "-100.00")
    private MonetaryAmount cancelledAmount;
    
    @Schema(description = "平台补偿金额", example = "-50.00")
    private MonetaryAmount compensationAmount;
    
    // ========== 基础成本 ==========
    @Schema(description = "采购成本", example = "-200.00")
    private MonetaryAmount purchaseCost;
    
    @Schema(description = "采购运费", example = "-15.00")
    private MonetaryAmount purchaseShipping;
    
    @Schema(description = "头程运费", example = "-25.00")
    private MonetaryAmount firstMileShipping;
    
    // ========== 仓储费用 ==========
    @Schema(description = "FBS仓储费", example = "-8.00")
    private MonetaryAmount fbsStorageFee;
    
    @Schema(description = "FBO送仓费", example = "-12.00")
    private MonetaryAmount fboDeliveryFee;
    
    @Schema(description = "FBO验收费", example = "-6.00")
    private MonetaryAmount fboInspectionFee;
    
    // ========== 税费 ==========
    @Schema(description = "销售VAT", example = "-100.00")
    private MonetaryAmount salesVat;
    
    @Schema(description = "关税", example = "-40.00")
    private MonetaryAmount customsDuty;
    
    @Schema(description = "进口VAT", example = "-60.00")
    private MonetaryAmount importVat;
    
    // ========== 汇总 ==========
    @Schema(description = "成本合计", example = "-581.00")
    private MonetaryAmount totalCost;
}