package cn.iocoder.yudao.module.chrome.controller.admin.credits.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户积分账户 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserCreditsRespVO {

    @Schema(description = "账户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21457")
    @ExcelProperty("账户ID")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6269")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "总积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("总积分")
    private Integer totalCredits;

    @Schema(description = "已使用积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已使用积分")
    private Integer usedCredits;

    @Schema(description = "剩余积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("剩余积分")
    private Integer remainingCredits;

    @Schema(description = "上次重置时间")
    @ExcelProperty("上次重置时间")
    private LocalDateTime lastResetTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}