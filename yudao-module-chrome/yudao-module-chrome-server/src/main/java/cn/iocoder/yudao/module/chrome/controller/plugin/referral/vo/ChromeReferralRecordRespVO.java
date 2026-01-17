package cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Chrome插件 - 推广佣金记录 Response VO")
@Data
public class ChromeReferralRecordRespVO {

    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "被推广者用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long inviteeUserId;

    @Schema(description = "订单金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.00")
    private BigDecimal orderAmount;

    @Schema(description = "佣金金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "9.90")
    private BigDecimal commissionAmount;

    @Schema(description = "状态：10-待结算, 20-已结算", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
