package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * 利润指标
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 利润指标")
@Data
@Builder
public class ProfitMetrics {
    
    @Schema(description = "毛利润", example = "419.00")
    private MonetaryAmount grossProfit;
    
    @Schema(description = "毛利率", example = "41.90")
    private BigDecimal grossProfitRate;
    
    @Schema(description = "净利润", example = "219.00")
    private MonetaryAmount netProfit;
    
    @Schema(description = "净利率", example = "21.90")
    private BigDecimal netProfitRate;
    
    @Schema(description = "投资回报率(ROI)", example = "109.50")
    private BigDecimal roi;
    
    @Schema(description = "单件毛利", example = "4.19")
    private MonetaryAmount unitGrossProfit;
    
    @Schema(description = "单件净利", example = "2.19")
    private MonetaryAmount unitNetProfit;
    
    @Schema(description = "成本率", example = "58.10")
    private BigDecimal costRate;
} 