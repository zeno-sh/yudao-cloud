package cn.iocoder.yudao.module.chrome.controller.plugin.credits.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Chrome插件 - 消耗积分 Response VO")
@Data
public class ConsumeCreditsRespVO {

    @Schema(description = "是否成功", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean success;

    @Schema(description = "消耗的积分数", example = "3")
    private Integer creditsConsumed;

    @Schema(description = "剩余积分数", example = "97")
    private Integer remainingCredits;

    @Schema(description = "功能类型名称", example = "商品采集")
    private String featureTypeName;

    @Schema(description = "消息", example = "积分消耗成功")
    private String message;

}
