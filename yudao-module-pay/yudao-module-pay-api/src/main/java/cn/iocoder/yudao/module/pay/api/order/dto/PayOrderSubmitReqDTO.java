package cn.iocoder.yudao.module.pay.api.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 支付订单提交 Request DTO
 */
@Data
public class PayOrderSubmitReqDTO {

    @Schema(description = "支付单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "支付单编号不能为空")
    private Long id;

    @Schema(description = "支付渠道", requiredMode = Schema.RequiredMode.REQUIRED, example = "wx_pub")
    @NotEmpty(message = "支付渠道不能为空")
    private String channelCode;

    @Schema(description = "支付渠道的额外参数，例如说，微信公众号需要传递 openid 参数")
    private Map<String, String> channelExtras;

    @Schema(description = "展示模式", example = "url")
    private String displayMode;

    @Schema(description = "回跳地址")
    @URL(message = "回跳地址的格式必须是 URL")
    private String returnUrl;

    @Schema(description = "用户 IP", requiredMode = Schema.RequiredMode.REQUIRED, example = "127.0.0.1")
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

}
