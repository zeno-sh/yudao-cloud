package cn.iocoder.yudao.module.chrome.controller.admin.plan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 订阅套餐配置 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SubscriptionPlanRespVO {

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13835")
    @ExcelProperty("套餐ID")
    private Long id;

    @Schema(description = "套餐名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("套餐名称")
    private String planName;

    @Schema(description = "平台类型（Amazon, Coupang等）", requiredMode = Schema.RequiredMode.REQUIRED, example = "Amazon")
    @ExcelProperty("平台类型")
    private String platform;

    @Schema(description = "订阅类型（10免费版 20基础版 30高级版）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("订阅类型（10免费版 20基础版 30高级版）")
    private Integer subscriptionType;

    @Schema(description = "计费周期（10月付 20年付）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计费周期（10月付 20年付）")
    private Integer billingCycle;

    @Schema(description = "积分数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("积分数量")
    private Integer credits;

    @Schema(description = "价格（美元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "9761")
    @ExcelProperty("价格（美元）")
    private BigDecimal price;

    @Schema(description = "折扣率（百分比）", example = "10.50")
    @ExcelProperty("折扣率（%）")
    private BigDecimal discountRate;

    @Schema(description = "折扣后价格", example = "8764.90")
    @ExcelProperty("折扣后价格")
    private BigDecimal discountedPrice;

    @Schema(description = "货币单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("货币单位")
    private String currency;

    @Schema(description = "状态（1启用 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态（1启用 0禁用）")
    private Boolean status;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序")
    private Integer sortOrder;

    @Schema(description = "套餐描述", example = "你说的对")
    @ExcelProperty("套餐描述")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}