package cn.iocoder.yudao.module.chrome.controller.plugin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Chrome 认证 - 注册 Response VO
 *
 * @author Jax
 */
@Schema(description = "Chrome 认证 - 注册 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChromeAuthRegisterRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "访问令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "happy")
    private String accessToken;

    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "nice")
    private String refreshToken;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expiresTime;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    private String email;

    @Schema(description = "昵称", example = "用户昵称")
    private String nickname;

    @Schema(description = "订阅状态", example = "1")
    private Integer subscriptionStatus;

    @Schema(description = "订阅类型", example = "1")
    private Integer subscriptionType;

    @Schema(description = "订阅过期时间")
    private LocalDateTime subscriptionExpireTime;

}