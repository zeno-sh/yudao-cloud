package cn.iocoder.yudao.module.dm.controller.admin.ad.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * Ozon广告同步任务分页查询 Request VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - Ozon广告同步任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OzonAdSyncTaskPageReqVO extends PageParam {

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;

    @Schema(description = "客户端ID", example = "123456")
    private String clientId;

    @Schema(description = "任务状态", example = "1")
    private Integer status;

    @Schema(description = "报告UUID", example = "report_123456")
    private String reportUuid;

    @Schema(description = "开始日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] beginDate;

    @Schema(description = "结束日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] endDate;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] finishTime;

    @Schema(description = "重试次数范围", example = "[0, 5]")
    private Integer[] retryCount;

    @Schema(description = "处理记录数范围", example = "[0, 1000]")
    private Integer[] processedCount;

    @Schema(description = "错误信息关键字", example = "timeout")
    private String errorMessage;

    @Schema(description = "备注关键字", example = "定时任务")
    private String remark;

}