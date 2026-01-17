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
 * Chrome 认证 - 注册 Request VO
 *
 * @author Jax
 */
@Schema(description = "Chrome 认证 - 注册 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChromeAuthRegisterReqVO {

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotEmpty(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为 6-20 位")
    private String password;

    @Schema(description = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    // TODO：客户端bug，没有上传
    // @NotEmpty(message = "确认密码不能为空")
    private String confirmPassword;

    @Schema(description = "昵称", example = "用户昵称")
    @Size(max = 30, message = "昵称长度不能超过 30 个字符")
    private String nickname;

    @Schema(description = "邮箱验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotEmpty(message = "邮箱验证码不能为空")
    private String emailCode;

    @Schema(description = "设备令牌", example = "device_token_123")
    private String deviceToken;

    @Schema(description = "推广码", example = "ABC1234")
    private String referralCode;

}