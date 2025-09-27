package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 利润报告查询请求（新版）
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 利润报告查询请求（V2版本）")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProfitReportQueryReqVO extends PageParam {
    
    @Schema(description = "任务ID（可选，用于查询特定任务的结果）", example = "abc123")
    private String taskId;
    
    @Schema(description = "门店ID", example = "client1")
    private String clientId;
    
    @Schema(description = "日期范围", example = "[\"2024-01-01\", \"2024-01-31\"]")
    private LocalDate[] dateRange;
    
    @Schema(description = "查询维度", example = "SKU")
    private String dimension;
    
    @Schema(description = "产品SKU（可选筛选）", example = "SKU123")
    private String platformSkuId;
    
    @Schema(description = "排序字段", example = "salesAmount")
    private String orderBy;
} 