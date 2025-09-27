package cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * Ozon 供应订单统计 DO
 */
@Data
@Accessors(chain = true)
public class OzonSupplyOrderStatsDO {

    /**
     * 供应订单编号
     */
    private Long supplyOrderId;

    /**
     * 商品总数量
     */
    private Integer totalItems;

    /**
     * 总体积
     */
    private BigDecimal totalVolume;

    /**
     * SKU数量
     */
    private Integer skuCount;

} 