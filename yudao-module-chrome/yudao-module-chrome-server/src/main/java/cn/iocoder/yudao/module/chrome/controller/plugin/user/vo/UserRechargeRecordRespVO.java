package cn.iocoder.yudao.module.chrome.controller.plugin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Chrome插件 - 用户充值记录 Response VO")
@Data
public class UserRechargeRecordRespVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20230101123456789")
    private String orderNo;

    @Schema(description = "套餐名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "高级版(月付)")
    private String planName;

    @Schema(description = "支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.00")
    private BigDecimal payPrice;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "订阅类型", example = "30")
    private Integer subscriptionType;

    @Schema(description = "计费周期", example = "1")
    private Integer billingCycle;
}
