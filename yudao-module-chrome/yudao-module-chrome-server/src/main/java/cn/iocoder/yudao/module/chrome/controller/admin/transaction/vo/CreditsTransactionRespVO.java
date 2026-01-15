package cn.iocoder.yudao.module.chrome.controller.admin.transaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分交易记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CreditsTransactionRespVO {

    @Schema(description = "交易ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19840")
    @ExcelProperty("交易ID")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31388")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "用户邮箱", example = "user@example.com")
    @ExcelProperty("用户邮箱")
    private String userEmail;

    @Schema(description = "交易类型（10充值 20消费 30赠送）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("交易类型（10充值 20消费 30赠送）")
    private Integer transactionType;

    @Schema(description = "积分数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("积分数量")
    private Integer creditsAmount;

    @Schema(description = "交易前积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("交易前积分")
    private Integer beforeCredits;

    @Schema(description = "交易后积分", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("交易后积分")
    private Integer afterCredits;

    @Schema(description = "业务类型（消费时关联功能类型）", example = "1")
    @ExcelProperty("业务类型（消费时关联功能类型）")
    private Integer businessType;

    @Schema(description = "业务ID（订单ID或使用记录ID）", example = "18984")
    @ExcelProperty("业务ID（订单ID或使用记录ID）")
    private String businessId;

    @Schema(description = "交易描述", example = "你猜")
    @ExcelProperty("交易描述")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}