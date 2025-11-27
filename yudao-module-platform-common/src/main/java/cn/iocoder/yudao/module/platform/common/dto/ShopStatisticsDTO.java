package cn.iocoder.yudao.module.platform.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 店铺统计数据 DTO
 * <p>
 * 统一的多平台店铺数据结构，各平台（Amazon、Coupang、Ozon等）必须返回此格式
 * </p>
 *
 * @author Jax
 */
@Data
@Accessors(chain = true)
@Schema(description = "店铺统计数据")
public class ShopStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 基础信息 ====================

    @Schema(description = "平台ID", example = "10")
    private Integer platformId;

    @Schema(description = "店铺ID", example = "123456")
    private String shopId;

    @Schema(description = "店铺名称", example = "我的店铺")
    private String shopName;

    @Schema(description = "平台名称", example = "Amazon")
    private String platformName;

    @Schema(description = "站点ID", example = "ATVPDKIKX0DER")
    private String marketplaceId;

    @Schema(description = "日期", example = "2024-01-01")
    private String nowDate;

    @Schema(description = "币种", example = "USD")
    private String currency;

    // ==================== 销售数据 ====================

    @Schema(description = "销量")
    private BigDecimal saleNum;

    @Schema(description = "订单量")
    private BigDecimal orderNum;

    @Schema(description = "销售额")
    private BigDecimal salePrice;

    // ==================== 退款/退货数据 ====================

    @Schema(description = "退款量")
    private BigDecimal refundNum;

    @Schema(description = "退款率")
    private BigDecimal refundRate;

    @Schema(description = "退货量")
    private BigDecimal returnSaleNum;

    @Schema(description = "退货率")
    private BigDecimal returnRate;

    // ==================== 流量数据 ====================

    @Schema(description = "Sessions")
    private BigDecimal sessions;

    @Schema(description = "PV(页面浏览量)")
    private BigDecimal pageView;

    @Schema(description = "转化率")
    private BigDecimal convertRate;

    // ==================== 广告数据 ====================

    @Schema(description = "广告曝光量")
    private BigDecimal adImpressions;

    @Schema(description = "广告点击量")
    private BigDecimal adClicks;

    @Schema(description = "广告转化率")
    private BigDecimal adConvertRate;

    @Schema(description = "广告点击率")
    private BigDecimal adClickRate;

    @Schema(description = "广告花费")
    private BigDecimal adCostSum;

    @Schema(description = "平均转化费(CPA)")
    private BigDecimal cpa;

    @Schema(description = "CPC(每次点击成本)")
    private BigDecimal cpc;

    @Schema(description = "广告订单量")
    private BigDecimal adOrderNum;

    @Schema(description = "广告订单量占比")
    private BigDecimal adOrderNumRate;

    @Schema(description = "广告销售额")
    private BigDecimal adSalesPrice;

    @Schema(description = "ACoS(广告成本销售比)")
    private BigDecimal acos;

    @Schema(description = "ACoAS")
    private BigDecimal acoas;

    @Schema(description = "ASoAS")
    private BigDecimal asoas;

    @Schema(description = "ROAS(广告投资回报率)")
    private BigDecimal roas;

    // ==================== 成本/利润数据 ====================

    @Schema(description = "采购成本")
    private BigDecimal purchaseCost;

    @Schema(description = "头程费用")
    private BigDecimal headTripPrice;

    @Schema(description = "毛利润")
    private BigDecimal profit;

    @Schema(description = "毛利率")
    private BigDecimal profitRate;

    // ==================== 库存数据 ====================

    @Schema(description = "FBA可售库存")
    private BigDecimal fbaAvailable;

}
