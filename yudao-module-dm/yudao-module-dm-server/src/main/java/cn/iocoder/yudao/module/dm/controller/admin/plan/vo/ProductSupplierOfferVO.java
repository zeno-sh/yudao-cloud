package cn.iocoder.yudao.module.dm.controller.admin.plan.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Zeno
 * @createTime: 2024/04/19 20:48
 */
@Data
public class ProductSupplierOfferVO {

    private Long id;

    /**
     * 报价
     */
    private BigDecimal price;
}
