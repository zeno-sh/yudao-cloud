package cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Chrome 认证 - 发送邮箱验证码 Request VO
 *
 * @author Jax
 */
@Schema(description = "Chrome 认证 - 发送邮箱验证码 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChromeAuthSendEmailCodeReqVO {

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    @Schema(description = "场景", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "场景不能为空")
    private Integer scene;

}