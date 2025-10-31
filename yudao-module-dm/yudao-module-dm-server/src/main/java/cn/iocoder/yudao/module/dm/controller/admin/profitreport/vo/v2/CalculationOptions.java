package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 计算选项
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 计算选项")
@Data
public class CalculationOptions {
    
    @Schema(description = "是否包含退货数据", example = "true")
    private Boolean includeReturns = true;
    
    @Schema(description = "是否包含取消订单", example = "false")
    private Boolean includeCancelled = false;
    
    @Schema(description = "是否并行计算", example = "true")
    private Boolean enableParallel = true;
    
    @Schema(description = "是否使用缓存", example = "true")
    private Boolean enableCache = true;
    
    @Schema(description = "计算精度（小数位数）", example = "4")
    private Integer precision = 4;
    
    @Schema(description = "显示精度（小数位数）", example = "2")
    private Integer displayPrecision = 2;
} 