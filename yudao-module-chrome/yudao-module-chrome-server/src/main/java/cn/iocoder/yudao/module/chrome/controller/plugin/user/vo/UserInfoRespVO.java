package cn.iocoder.yudao.module.chrome.controller.plugin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "Chrome插件 - 用户信息 Response VO")
@Data
public class UserInfoRespVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3609")
    private Long id;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "状态（1正常 0禁用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean status;

    @Schema(description = "最后登录时间")
    private LocalDateTime loginDate;

    // 订阅信息
    @Schema(description = "套餐类型（10免费版 20基础版 30高级版）")
    private Integer subscriptionType;

    @Schema(description = "计费周期（10月 20年）")
    private Integer billingCycle;

    @Schema(description = "套餐类型名称")
    private String subscriptionTypeName;

    @Schema(description = "订阅状态（1有效 0无效）")
    private Boolean subscriptionStatus;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "到期时间")
    private LocalDateTime endTime;

    // 积分信息
    @Schema(description = "总积分")
    private Integer totalCredits;

    @Schema(description = "已使用积分")
    private Integer usedCredits;

    @Schema(description = "剩余积分")
    private Integer remainingCredits;

    @Schema(description = "上次积分重置时间")
    private LocalDateTime lastResetTime;

    // 使用统计
    @Schema(description = "今日使用次数")
    private Integer todayUsageCount;

    @Schema(description = "本月使用次数")
    private Integer monthUsageCount;

    @Schema(description = "总使用次数")
    private Integer totalUsageCount;

}
