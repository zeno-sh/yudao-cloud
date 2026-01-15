package cn.iocoder.yudao.module.chrome.controller.admin.email.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - Chrome邮箱验证码 Response VO")
@Data
@ExcelIgnoreUnannotated
public class EmailCodeRespVO {

    @Schema(description = "验证码ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28972")
    @ExcelProperty("验证码ID")
    private Long id;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("邮箱")
    private String email;

    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("验证码")
    private String code;

    @Schema(description = "场景（10注册 20忘记密码 30修改密码）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("场景（10注册 20忘记密码 30修改密码）")
    private Integer scene;

    @Schema(description = "是否已使用", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否已使用")
    private Boolean used;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}