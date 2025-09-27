package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 财务账单报告计算任务日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProfitReportTaskLogPageReqVO extends PageParam {

    @Schema(description = "任务ID", example = "a1b2c3d4e5")
    private String taskId;

    @Schema(description = "查询维度：sku、client", example = "sku")
    private String dimension;

    @Schema(description = "时间类型：day、week、month", example = "day")
    private String timeType;

    @Schema(description = "执行状态：0-执行中，1-执行成功，2-执行失败", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
} 