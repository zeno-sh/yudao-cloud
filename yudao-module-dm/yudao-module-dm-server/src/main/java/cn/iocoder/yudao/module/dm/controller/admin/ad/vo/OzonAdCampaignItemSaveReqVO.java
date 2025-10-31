package cn.iocoder.yudao.module.dm.controller.admin.ad.vo;

import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 广告活动新增/修改 Request VO")
@Data
public class OzonAdCampaignItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5183")
    private Integer id;

    @Schema(description = "门店ID", example = "8817")
    private String clientId;

    @Schema(description = "活动ID", example = "4851")
    private String campaignId;

    @Schema(description = "平台Sku")
    private String platformSkuId;

    @Schema(description = "展示量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer views;

    @Schema(description = "点击数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer clicks;

    @Schema(description = "点击数", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal cr;

    @Schema(description = "广告花费", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal moneySpent;

    @Schema(description = "平均报价", requiredMode = Schema.RequiredMode.REQUIRED, example = "29986")
    private BigDecimal avgBid;

    @Schema(description = "订单数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer orders;

    @Schema(description = "订单金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal ordersMoney;

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "广告明细列表")
    private List<OzonAdCampaignsItemDO> ozonAdCampaignsItems;

    @Schema(description = "日期")
    private BigDecimal price;

    @Schema(description = "平台订单ID，搜索广告时有值")
    private String orderId;

}