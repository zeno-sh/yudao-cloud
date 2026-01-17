package cn.iocoder.yudao.module.chrome.controller.plugin.referral.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "Chrome插件 - 推广信息 Response VO")
@Data
public class ChromeReferralInfoRespVO {

    @Schema(description = "我的推广码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ABC1234")
    private String code;

    @Schema(description = "累计邀请人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long inviteeCount;

    @Schema(description = "累计获得佣金", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal totalCommission;

}
