package cn.iocoder.yudao.module.chrome.controller.plugin.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "Chrome插件 - 订阅套餐列表 Response VO")
@Data
public class SubscriptionPlanListRespVO {

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13835")
    private Long id;

    @Schema(description = "套餐名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "基础版")
    private String planName;

    @Schema(description = "订阅类型（10免费版 20基础版 30高级版）", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer subscriptionType;

    @Schema(description = "计费周期（10月付 20年付）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer billingCycle;

    @Schema(description = "积分数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer credits;

    @Schema(description = "价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "9.99")
    private BigDecimal price;

    @Schema(description = "折扣率（百分比）", example = "10.50")
    private BigDecimal discountRate;

    @Schema(description = "折扣后价格", example = "8.99")
    private BigDecimal discountedPrice;

    @Schema(description = "货币单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "USD")
    private String currency;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sortOrder;

    @Schema(description = "套餐描述", example = "适合个人用户的基础套餐")
    private String description;

}
