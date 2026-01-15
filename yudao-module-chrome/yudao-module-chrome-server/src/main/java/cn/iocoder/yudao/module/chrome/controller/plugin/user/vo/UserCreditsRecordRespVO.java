package cn.iocoder.yudao.module.chrome.controller.plugin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "Chrome插件 - 用户积分记录 Response VO")
@Data
public class UserCreditsRecordRespVO {

    @Schema(description = "交易ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19840")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31388")
    private Long userId;

    @Schema(description = "交易类型（10充值 20消费 30赠送 40重置 50API调用失败 60API调用无数据）", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer transactionType;

    @Schema(description = "交易类型名称", example = "消费")
    private String transactionTypeName;

    @Schema(description = "积分数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer creditsAmount;

    @Schema(description = "交易前积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer beforeCredits;

    @Schema(description = "交易后积分", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer afterCredits;

    @Schema(description = "业务类型（消费时关联功能类型）", example = "1")
    private Integer businessType;

    @Schema(description = "业务ID（订单ID或使用记录ID）", example = "18984")
    private String businessId;

    @Schema(description = "交易描述", example = "查询商品评论消费积分")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
