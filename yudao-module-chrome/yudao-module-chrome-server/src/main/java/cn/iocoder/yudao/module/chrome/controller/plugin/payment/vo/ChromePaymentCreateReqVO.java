package cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Chrome 创建支付订单 Request VO
 *
 * @author Jax
 */
@Schema(description = "插件端 - Chrome 创建支付订单 Request VO")
@Data
public class ChromePaymentCreateReqVO {

    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    @NotBlank(message = "用户邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "套餐ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "套餐ID不能为空")
    private Long planId;

    @Schema(description = "支付渠道代码", example = "alipay_pc")
    private String channelCode;

}
