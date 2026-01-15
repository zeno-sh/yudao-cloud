package cn.iocoder.yudao.module.chrome.controller.plugin.credits.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "Chrome插件 - 消耗积分 Request VO")
@Data
public class ConsumeCreditsReqVO {

    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "test@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "功能类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "功能类型不能为空")
    private Integer featureType;

}
