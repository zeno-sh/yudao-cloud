package cn.iocoder.yudao.module.chrome.controller.admin.credits.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户积分账户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserCreditsPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "6269")
    private Long userId;

    @Schema(description = "总积分")
    private Integer totalCredits;

    @Schema(description = "已使用积分")
    private Integer usedCredits;

    @Schema(description = "剩余积分")
    private Integer remainingCredits;

    @Schema(description = "上次重置时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] lastResetTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}