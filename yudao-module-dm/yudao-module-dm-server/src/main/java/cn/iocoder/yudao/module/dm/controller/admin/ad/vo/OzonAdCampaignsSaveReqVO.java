package cn.iocoder.yudao.module.dm.controller.admin.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;

@Schema(description = "管理后台 - 广告活动新增/修改 Request VO")
@Data
public class OzonAdCampaignsSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5183")
    private Integer id;

    private Long tenantId;

    @Schema(description = "门店ID", example = "8817")
    private String clientId;

    @Schema(description = "活动ID", example = "4851")
    private String campaignId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "展示量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "展示量不能为空")
    private Integer views;

    @Schema(description = "点击数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "点击数不能为空")
    private Integer clicks;

    @Schema(description = "广告花费", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "广告花费不能为空")
    private BigDecimal moneySpent;

    @Schema(description = "平均报价", requiredMode = Schema.RequiredMode.REQUIRED, example = "29986")
    @NotNull(message = "平均报价不能为空")
    private BigDecimal avgBid;

    @Schema(description = "订单数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单数量不能为空")
    private Integer orders;

    @Schema(description = "订单金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单金额不能为空")
    private BigDecimal ordersMoney;

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "广告明细列表")
    private List<OzonAdCampaignsItemDO> ozonAdCampaignsItems;

}