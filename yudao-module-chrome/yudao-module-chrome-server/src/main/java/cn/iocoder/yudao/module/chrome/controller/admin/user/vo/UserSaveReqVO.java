package cn.iocoder.yudao.module.chrome.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户新增/修改 Request VO")
@Data
public class UserSaveReqVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3609")
    private Long id;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "邮箱不能为空")
    private String email;

    @Schema(description = "密码（加密）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "密码（加密）不能为空")
    private String password;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "状态（1正常 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态（1正常 0禁用）不能为空")
    private Boolean status;

    @Schema(description = "最后登录IP")
    private String loginIp;

    @Schema(description = "最后登录时间")
    private LocalDateTime loginDate;

    @Schema(description = "设备令牌（用于单设备登录）")
    private String deviceToken;

}