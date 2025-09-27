package cn.iocoder.yudao.module.dm.controller.admin.ad.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ozon广告同步任务响应 VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - Ozon广告同步任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OzonAdSyncTaskRespVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("任务ID")
    private Long id;

    @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("租户ID")
    private Long tenantId;

    @Schema(description = "客户端ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @ExcelProperty("客户端ID")
    private String clientId;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("开始日期")
    private LocalDate beginDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("结束日期")
    private LocalDate endDate;

    @Schema(description = "任务状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "任务状态", converter = DictConvert.class)
    @DictFormat("ozon_ad_sync_task_status")
    private Integer status;

    @Schema(description = "报告UUID", example = "report_123456")
    @ExcelProperty("报告UUID")
    private String reportUuid;

    @Schema(description = "重试次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("重试次数")
    private Integer retryCount;

    @Schema(description = "最大重试次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @ExcelProperty("最大重试次数")
    private Integer maxRetryCount;

    @Schema(description = "下次重试时间")
    @ExcelProperty("下次重试时间")
    private LocalDateTime nextRetryTime;

    @Schema(description = "错误信息")
    @ExcelProperty("错误信息")
    private String errorMessage;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "完成时间")
    @ExcelProperty("完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "处理记录数", example = "100")
    @ExcelProperty("处理记录数")
    private Integer processedCount;

    @Schema(description = "任务参数")
    @ExcelProperty("任务参数")
    private String taskParams;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    // 扩展字段
    @Schema(description = "任务耗时（秒）", example = "120")
    @ExcelProperty("任务耗时（秒）")
    private Long durationSeconds;

    @Schema(description = "任务状态描述", example = "处理中")
    @ExcelProperty("任务状态描述")
    private String statusDesc;

    @Schema(description = "是否可重试", example = "true")
    private Boolean canRetry;

    @Schema(description = "是否可取消", example = "true")
    private Boolean canCancel;

}