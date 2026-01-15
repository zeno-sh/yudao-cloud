package cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Chrome 支付订单提交 Response VO
 *
 * @author Jax
 */
@Schema(description = "插件端 - Chrome 支付订单提交 Response VO")
@Data
public class ChromePaymentSubmitRespVO {

    @Schema(description = "支付状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer status;

    @Schema(description = "展示模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "url")
    private String displayMode;

    @Schema(description = "展示内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://openapi.alipay.com/gateway.do?...")
    private String displayContent;

}
