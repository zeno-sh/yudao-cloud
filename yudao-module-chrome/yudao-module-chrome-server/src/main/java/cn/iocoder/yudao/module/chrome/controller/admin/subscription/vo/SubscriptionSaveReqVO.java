package cn.iocoder.yudao.module.chrome.controller.admin.subscription.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 插件订阅新增/修改 Request VO")
@Data
public class SubscriptionSaveReqVO {

    @Schema(description = "订阅ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20953")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2954")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "订阅类型（10免费版 20基础版 30高级版", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "订阅类型（10免费版 20基础版 30高级版不能为空")
    private Integer subscriptionType;

    @Schema(description = "状态（1有效 0无效）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态（1有效 0无效）不能为空")
    private Boolean status;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "付费时长（天数）")
    private Integer paymentDuration;

    @Schema(description = "是否自动续费", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否自动续费不能为空")
    private Boolean autoRenew;

}