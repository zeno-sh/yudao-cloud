package cn.iocoder.yudao.module.chrome.controller.admin.credits.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户积分账户新增/修改 Request VO")
@Data
public class UserCreditsSaveReqVO {

    @Schema(description = "账户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21457")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6269")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "总积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "总积分不能为空")
    private Integer totalCredits;

    @Schema(description = "已使用积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "已使用积分不能为空")
    private Integer usedCredits;

    @Schema(description = "剩余积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "剩余积分不能为空")
    private Integer remainingCredits;

    @Schema(description = "上次重置时间")
    private LocalDateTime lastResetTime;

}