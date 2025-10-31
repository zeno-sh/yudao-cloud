package cn.iocoder.yudao.module.dm.controller.admin.plan.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Zeno
 * @createTime: 2024/04/19 19:09
 */
@Data
public class ProductPlatformTrendVO {

    /**
     * 产品Id
     */
    private Long productId;
    /**
     * 平台销量
     */
    private Integer competitorSaleNumber;
    /**
     * 竞品产品Id
     */
    private String competitorSkuId;
    /**
     * 划线价
     */
    private BigDecimal competitorSalePrice;
}
