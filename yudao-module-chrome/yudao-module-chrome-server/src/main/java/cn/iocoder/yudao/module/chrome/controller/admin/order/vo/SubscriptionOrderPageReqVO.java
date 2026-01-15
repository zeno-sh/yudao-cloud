package cn.iocoder.yudao.module.chrome.controller.admin.order.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 订阅订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubscriptionOrderPageReqVO extends PageParam {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID", example = "10717")
    private Long userId;

    @Schema(description = "套餐ID", example = "10740")
    private Long planId;

    @Schema(description = "订阅类型", example = "2")
    private Integer subscriptionType;

    @Schema(description = "计费周期")
    private Integer billingCycle;

    @Schema(description = "积分数量")
    private Integer credits;

    @Schema(description = "原价", example = "21298")
    private BigDecimal originalPrice;

    @Schema(description = "实付金额", example = "32531")
    private BigDecimal actualPrice;

    @Schema(description = "货币单位")
    private String currency;

    @Schema(description = "支付方式 （10微信 20支付宝 30其他）")
    private Integer paymentMethod;

    @Schema(description = "支付状态（10待支付 20已支付 30已取消 40已退款）", example = "2")
    private Integer paymentStatus;

    @Schema(description = "支付时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] paymentTime;

    @Schema(description = "过期时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expireTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}