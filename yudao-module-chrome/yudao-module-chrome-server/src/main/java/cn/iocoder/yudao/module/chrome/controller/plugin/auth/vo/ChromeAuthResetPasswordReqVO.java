package cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

/**
 * Chrome 认证 - 重置密码 Request VO
 *
 * @author Jax
 */
@Schema(description = "Chrome 认证 - 重置密码 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChromeAuthResetPasswordReqVO {

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotEmpty(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为 6-20 位")
    private String newPassword;

    @Schema(description = "确认新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotEmpty(message = "确认新密码不能为空")
    private String confirmNewPassword;

    @Schema(description = "邮箱验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotEmpty(message = "邮箱验证码不能为空")
    private String emailCode;

}