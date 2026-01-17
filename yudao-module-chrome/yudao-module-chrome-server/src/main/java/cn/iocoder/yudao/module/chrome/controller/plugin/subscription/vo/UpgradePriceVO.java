package cn.iocoder.yudao.module.chrome.controller.plugin.subscription.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * 升级价格信息 VO
 */
@Schema(description = "插件端 - 升级价格信息")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpgradePriceVO {

    @Schema(description = "目标套餐ID", example = "4")
    private Long targetPlanId;

    @Schema(description = "目标套餐名称", example = "高级版-年卡")
    private String targetPlanName;

    @Schema(description = "目标套餐原价（元）", example = "2399.00")
    private BigDecimal originalPrice;

    @Schema(description = "当前订阅剩余价值（元）", example = "725.00")
    private BigDecimal remainingValue;

    @Schema(description = "升级需支付金额（元）", example = "1674.00")
    private BigDecimal upgradePrice;

    @Schema(description = "是否为续费（相同套餐）", example = "false")
    private Boolean isRenewal;

    @Schema(description = "是否为升级（允许购买）", example = "true")
    private Boolean isUpgrade;

    @Schema(description = "提示信息", example = "您将从基础版年卡升级到高级版年卡")
    private String message;

}
