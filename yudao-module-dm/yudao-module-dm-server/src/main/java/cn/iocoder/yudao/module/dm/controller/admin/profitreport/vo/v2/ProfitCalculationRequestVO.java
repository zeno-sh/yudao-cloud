package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 利润计算请求参数（新版）
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 利润计算请求参数（V2版本）")
@Data
public class ProfitCalculationRequestVO {
    
    @Schema(description = "门店ID", example = "7216")
    private String[] clientIds;

    @Schema(description = "账单日期")
    private String[] financeDate;
    
    @Schema(description = "查询维度：sku、client，默认为client", example = "client")
    private String dimension = "client";
    
    @Schema(description = "时间类型：day、week、month，默认为day", example = "day")
    private String timeType = "day";
} 