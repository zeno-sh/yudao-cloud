package cn.iocoder.yudao.module.chrome.controller.admin.transaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 积分交易记录新增/修改 Request VO")
@Data
public class CreditsTransactionSaveReqVO {

    @Schema(description = "交易ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19840")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31388")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "交易类型（10充值 20消费 30赠送）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "交易类型（10充值 20消费 30赠送）不能为空")
    private Integer transactionType;

    @Schema(description = "积分数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "积分数量不能为空")
    private Integer creditsAmount;

    @Schema(description = "交易前积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "交易前积分不能为空")
    private Integer beforeCredits;

    @Schema(description = "交易后积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "交易后积分不能为空")
    private Integer afterCredits;

    @Schema(description = "业务类型（消费时关联功能类型）", example = "1")
    private Integer businessType;

    @Schema(description = "业务ID（订单ID或使用记录ID）", example = "18984")
    private String businessId;

    @Schema(description = "交易描述", example = "你猜")
    private String description;

}