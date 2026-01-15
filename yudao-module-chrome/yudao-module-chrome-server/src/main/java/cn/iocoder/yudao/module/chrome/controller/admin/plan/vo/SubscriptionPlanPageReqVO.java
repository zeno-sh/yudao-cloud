package cn.iocoder.yudao.module.chrome.controller.admin.plan.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 订阅套餐配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubscriptionPlanPageReqVO extends PageParam {

    @Schema(description = "套餐名称", example = "芋艿")
    private String planName;

    @Schema(description = "订阅类型（10免费版 20基础版 30高级版）", example = "2")
    private Integer subscriptionType;

    @Schema(description = "计费周期（10月付 20年付）")
    private Integer billingCycle;

    @Schema(description = "积分数量")
    private Integer credits;

    @Schema(description = "价格（美元）", example = "9761")
    private BigDecimal price;

    @Schema(description = "货币单位")
    private String currency;

    @Schema(description = "状态（1启用 0禁用）", example = "1")
    private Boolean status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "套餐描述", example = "你说的对")
    private String description;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}