package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 任务初始化结果（新版）
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 任务初始化结果（V2版本）")
@Data
@Builder
public class TaskInitResultVO {
    
    @Schema(description = "任务ID", example = "abc123")
    private String taskId;
    
    @Schema(description = "初始状态", example = "0")
    private Integer status;
    
    @Schema(description = "预估处理时间（分钟）", example = "5")
    private Integer estimatedDuration;
    
    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createTime;
    
    @Schema(description = "计算范围描述", example = "店铺store1，2024-01-01至2024-01-31，SKU维度")
    private String calculationScope;
} 