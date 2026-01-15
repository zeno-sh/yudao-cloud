package cn.iocoder.yudao.module.chrome.controller.admin.order.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 订阅订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SubscriptionOrderRespVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17555")
    @ExcelProperty("订单ID")
    private Long id;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("订单号")
    private String orderNo;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10717")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "用户邮箱", example = "user@example.com")
    @ExcelProperty("用户邮箱")
    private String userEmail;

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10740")
    @ExcelProperty("套餐ID")
    private Long planId;

    @Schema(description = "套餐名称", example = "基础套餐")
    @ExcelProperty("套餐名称")
    private String planName;

    @Schema(description = "订阅类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("订阅类型")
    private Integer subscriptionType;

    @Schema(description = "计费周期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计费周期")
    private Integer billingCycle;

    @Schema(description = "积分数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("积分数量")
    private Integer credits;

    @Schema(description = "原价", requiredMode = Schema.RequiredMode.REQUIRED, example = "21298")
    @ExcelProperty("原价")
    private BigDecimal originalPrice;

    @Schema(description = "实付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "32531")
    @ExcelProperty("实付金额")
    private BigDecimal actualPrice;

    @Schema(description = "货币单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("货币单位")
    private String currency;

    @Schema(description = "支付方式 （10微信 20支付宝 30其他）")
    @ExcelProperty("支付方式 （10微信 20支付宝 30其他）")
    private Integer paymentMethod;

    @Schema(description = "支付状态（10待支付 20已支付 30已取消 40已退款）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("支付状态（10待支付 20已支付 30已取消 40已退款）")
    private Integer paymentStatus;

    @Schema(description = "支付时间")
    @ExcelProperty("支付时间")
    private LocalDateTime paymentTime;

    @Schema(description = "过期时间")
    @ExcelProperty("过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}