package cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Chrome 支付订单提交 Request VO
 *
 * @author Jax
 */
@Schema(description = "插件端 - Chrome 支付订单提交 Request VO")
@Data
public class ChromePaymentSubmitReqVO {

    @Schema(description = "支付单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "支付单编号不能为空")
    private Long id;

    @Schema(description = "支付渠道", requiredMode = Schema.RequiredMode.REQUIRED, example = "alipay_wap")
    @NotEmpty(message = "支付渠道不能为空")
    private String channelCode;

    @Schema(description = "支付渠道的额外参数")
    private Map<String, String> channelExtras;

    @Schema(description = "展示模式", example = "url")
    private String displayMode;

    @Schema(description = "回跳地址", example = "https://your-domain.com/payment/return")
    @URL(message = "回跳地址的格式必须是 URL")
    private String returnUrl;

}
