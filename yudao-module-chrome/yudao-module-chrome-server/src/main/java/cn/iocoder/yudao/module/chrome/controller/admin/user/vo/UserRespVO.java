package cn.iocoder.yudao.module.chrome.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserRespVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3609")
    @ExcelProperty("用户ID")
    private Long id;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("邮箱")
    private String email;

    @Schema(description = "密码（加密）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("密码（加密）")
    private String password;

    @Schema(description = "昵称", example = "张三")
    @ExcelProperty("昵称")
    private String nickname;

    @Schema(description = "状态（1正常 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态（1正常 0禁用）")
    private Boolean status;

    @Schema(description = "最后登录IP")
    @ExcelProperty("最后登录IP")
    private String loginIp;

    @Schema(description = "最后登录时间")
    @ExcelProperty("最后登录时间")
    private LocalDateTime loginDate;

    @Schema(description = "设备令牌（用于单设备登录）")
    @ExcelProperty("设备令牌（用于单设备登录）")
    private String deviceToken;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}