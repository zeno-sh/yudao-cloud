package cn.iocoder.yudao.module.chrome.controller.admin.subscription.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 插件订阅 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SubscriptionRespVO {

    @Schema(description = "订阅ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20953")
    @ExcelProperty("订阅ID")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2954")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "订阅类型（10免费版 20基础版 30高级版", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("订阅类型（10免费版 20基础版 30高级版")
    private Integer subscriptionType;

    @Schema(description = "状态（1有效 0无效）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态（1有效 0无效）")
    private Boolean status;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @Schema(description = "付费时长（天数）")
    @ExcelProperty("付费时长（天数）")
    private Integer paymentDuration;

    @Schema(description = "是否自动续费", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否自动续费")
    private Boolean autoRenew;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long planId;

    @Schema(description = "计费周期（10月 20年）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer billingCycle;

}