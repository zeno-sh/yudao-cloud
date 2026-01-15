package cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Chrome 支付订单 Response VO
 *
 * @author Jax
 */
@Schema(description = "插件端 - Chrome 支付订单 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChromePaymentRespVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long orderId;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CHR1234567890")
    private String orderNo;

    @Schema(description = "支付订单ID（pay模块）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long payOrderId;

    @Schema(description = "支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "9.99")
    private BigDecimal actualPrice;

    @Schema(description = "货币单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "USD")
    private String currency;

    @Schema(description = "支付状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer paymentStatus;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expireTime;

}
