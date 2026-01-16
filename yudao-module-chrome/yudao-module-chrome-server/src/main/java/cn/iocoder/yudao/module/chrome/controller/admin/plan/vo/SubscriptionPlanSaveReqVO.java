package cn.iocoder.yudao.module.chrome.controller.admin.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 订阅套餐配置新增/修改 Request VO")
@Data
public class SubscriptionPlanSaveReqVO {

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13835")
    private Long id;

    @Schema(description = "套餐名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "套餐名称不能为空")
    private String planName;

    @Schema(description = "平台类型（Amazon, Coupang等）", requiredMode = Schema.RequiredMode.REQUIRED, example = "Amazon")
    @NotEmpty(message = "平台类型不能为空")
    private String platform;

    @Schema(description = "订阅类型（10免费版 20基础版 30高级版）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "订阅类型（10免费版 20基础版 30高级版）不能为空")
    private Integer subscriptionType;

    @Schema(description = "计费周期（10月付 20年付）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计费周期（10月付 20年付）不能为空")
    private Integer billingCycle;

    @Schema(description = "积分数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "积分数量不能为空")
    private Integer credits;

    @Schema(description = "订阅时长（天数，0表示不提供时长）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订阅时长不能为空")
    private Integer durationDays;

    @Schema(description = "价格（美元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "9761")
    @NotNull(message = "价格（美元）不能为空")
    private BigDecimal price;

    @Schema(description = "折扣率（百分比）", example = "10.50")
    private BigDecimal discountRate;

    @Schema(description = "折扣后价格", example = "8764.90")
    private BigDecimal discountedPrice;

    @Schema(description = "货币单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "货币单位不能为空")
    private String currency;

    @Schema(description = "状态（1启用 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态（1启用 0禁用）不能为空")
    private Boolean status;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sortOrder;

    @Schema(description = "套餐描述", example = "你说的对")
    private String description;

}