package cn.iocoder.yudao.module.chrome.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 订阅订单新增/修改 Request VO")
@Data
public class SubscriptionOrderSaveReqVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17555")
    private Long id;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "订单号不能为空")
    private String orderNo;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10717")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10740")
    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    @Schema(description = "订阅类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "订阅类型不能为空")
    private Integer subscriptionType;

    @Schema(description = "计费周期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计费周期不能为空")
    private Integer billingCycle;

    @Schema(description = "积分数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "积分数量不能为空")
    private Integer credits;

    @Schema(description = "原价", requiredMode = Schema.RequiredMode.REQUIRED, example = "21298")
    @NotNull(message = "原价不能为空")
    private BigDecimal originalPrice;

    @Schema(description = "实付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "32531")
    @NotNull(message = "实付金额不能为空")
    private BigDecimal actualPrice;

    @Schema(description = "货币单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "货币单位不能为空")
    private String currency;

    @Schema(description = "支付方式 （10微信 20支付宝 30其他）")
    private Integer paymentMethod;

    @Schema(description = "支付状态（10待支付 20已支付 30已取消 40已退款）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "支付状态（10待支付 20已支付 30已取消 40已退款）不能为空")
    private Integer paymentStatus;

    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

}