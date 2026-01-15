package cn.iocoder.yudao.module.chrome.controller.admin.transaction.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 积分交易记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CreditsTransactionPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "31388")
    private Long userId;

    @Schema(description = "交易类型（10充值 20消费 30赠送）", example = "2")
    private Integer transactionType;

    @Schema(description = "积分数量")
    private Integer creditsAmount;

    @Schema(description = "交易前积分")
    private Integer beforeCredits;

    @Schema(description = "交易后积分")
    private Integer afterCredits;

    @Schema(description = "业务类型（消费时关联功能类型）", example = "1")
    private Integer businessType;

    @Schema(description = "业务ID（订单ID或使用记录ID）", example = "18984")
    private String businessId;

    @Schema(description = "交易描述", example = "你猜")
    private String description;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}