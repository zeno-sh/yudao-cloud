package cn.iocoder.yudao.module.platform.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 产品统计数据 DTO
 * <p>
 * 统一的多平台产品维度数据结构，各平台（Amazon、Coupang、Ozon等）必须返回此格式
 * </p>
 *
 * @author Jax
 */
@Data
@Accessors(chain = true)
@Schema(description = "产品统计数据")
public class ProductStatisticsDTO implements Serializable {

    private static final long serialVersionUID = -3541141331462764720L;

    // ==================== 基础信息 ====================

    @Schema(description = "平台ID", example = "10")
    private Integer platformId;

    @Schema(description = "平台名称", example = "Amazon")
    private String platformName;

    @Schema(description = "店铺ID", example = "123456")
    private String shopId;

    @Schema(description = "店铺名称", example = "我的店铺")
    private String shopName;

    @Schema(description = "站点ID", example = "ATVPDKIKX0DER")
    private String marketplaceId;

    @Schema(description = "站点名称", example = "美国站")
    private String marketplaceName;

    @Schema(description = "币种", example = "USD")
    private String currency;

    @Schema(description = "日期", example = "2024-01-01")
    private String nowDate;

    // ==================== 产品信息 ====================

    @Schema(description = "父ASIN", example = "B08N5WRWNW")
    private String parentAsin;

    @Schema(description = "ASIN", example = "B08N5WRK9N")
    private List<String> asinList;

    @Schema(description = "商家SKU", example = "SKU-001")
    private String msku;

    @Schema(description = "本地SKU", example = "LOCAL-001")
    private String sku;

    @Schema(description = "产品名称", example = "无线蓝牙耳机")
    private String productName;

    @Schema(description = "产品图片URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    // ==================== 销售数据 ====================

    @Schema(description = "销量")
    private BigDecimal saleNum;

    @Schema(description = "订单量")
    private BigDecimal orderNum;

    @Schema(description = "销售额")
    private BigDecimal salePrice;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    // ==================== 退款/退货数据 ====================

    @Schema(description = "退款量")
    private BigDecimal refundNum;

    @Schema(description = "退款额")
    private BigDecimal refundPrice;

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

    @Schema(description = "广告点击率")
    private BigDecimal adClickRate;

    @Schema(description = "广告转化率")
    private BigDecimal adConvertRate;

    @Schema(description = "广告花费")
    private BigDecimal adCostSum;

    @Schema(description = "CPC(每次点击成本)")
    private BigDecimal cpc;

    @Schema(description = "CPA(平均转化费)")
    private BigDecimal cpa;

    @Schema(description = "广告订单量")
    private BigDecimal adOrderNum;

    @Schema(description = "广告订单量占比")
    private BigDecimal adOrderNumRate;

    @Schema(description = "广告销售额")
    private BigDecimal adSalesPrice;

    @Schema(description = "ACoS(广告成本销售比)")
    private BigDecimal acos;

    @Schema(description = "TACoS")
    private BigDecimal tacos;

    // ==================== 库存数据 ====================

    @Schema(description = "FBA可售库存")
    private Integer fbaAvailable;

    @Schema(description = "FBM可售库存")
    private Integer fbmAvailable;

    @Schema(description = "总可售库存")
    private Integer totalAvailable;

    @Schema(description = "预计可售天数")
    private Integer availableDays;

}
