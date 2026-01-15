package cn.iocoder.yudao.module.chrome.controller.admin.email.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - Chrome邮箱验证码新增/修改 Request VO")
@Data
public class EmailCodeSaveReqVO {

    @Schema(description = "验证码ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28972")
    private Long id;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "邮箱不能为空")
    private String email;

    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "验证码不能为空")
    private String code;

    @Schema(description = "场景（10注册 20忘记密码 30修改密码）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "场景（10注册 20忘记密码 30修改密码）不能为空")
    private Integer scene;

    @Schema(description = "是否已使用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否已使用不能为空")
    private Boolean used;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "过期时间不能为空")
    private LocalDateTime expireTime;

}