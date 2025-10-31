package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 财务账单报告计算任务日志 Response VO")
@Data
@ToString(callSuper = true)
public class ProfitReportTaskLogRespVO {

    @Schema(description = "日志ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "a1b2c3d4e5")
    private String taskId;

    @Schema(description = "门店ID数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "store1,store2")
    private String clientIds;

    @Schema(description = "查询维度", example = "sku")
    private String dimension;

    @Schema(description = "时间类型", example = "day")
    private String timeType;

    @Schema(description = "开始时间", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "结束时间", example = "2024-01-31")
    private LocalDate endDate;

    @Schema(description = "任务执行开始时间")
    private LocalDateTime executeStartTime;

    @Schema(description = "任务执行结束时间")
    private LocalDateTime executeEndTime;

    @Schema(description = "执行状态：0-执行中，1-执行成功，2-执行失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorInfo;

    @Schema(description = "执行日志")
    private String executeLog;

    @Schema(description = "影响记录数", example = "1000")
    private Integer affectedRecords;
    
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
} 