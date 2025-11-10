package cn.iocoder.yudao.module.dm.controller.admin.report.vo;

import cn.iocoder.yudao.framework.common.pojo.SortablePageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * SKU报表查询请求 VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - SKU报表查询请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SkuReportQueryReqVO extends SortablePageParam {
    
    @Schema(description = "产品ID列表", example = "[1, 2, 3]")
    private List<Long> productIds;
    
    @Schema(description = "SKU列表", example = "[\"ABC-111\", \"XYZ-222\"]")
    private List<String> skuIds;
    
    @Schema(description = "查询销量统计的开始时间", example = "2025-01-01")
    private LocalDate startTime;
    
    @Schema(description = "查询销量统计的结束时间", example = "2025-01-07")
    private LocalDate endTime;
    
    @Schema(description = "店铺ID（用于订单查询）", example = "SHOP001")
    private String shopId;
    
    @Schema(description = "平台（用于订单查询）", example = "US")
    private String marketplace;
}
