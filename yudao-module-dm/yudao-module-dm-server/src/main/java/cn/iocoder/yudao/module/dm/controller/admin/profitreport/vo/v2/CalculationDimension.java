package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计算维度枚举
 *
 * @author Jax
 */
@Getter
@AllArgsConstructor
public enum CalculationDimension {

    SKU("SKU维度", "按产品SKU进行利润计算"),
    CLIENT("门店维度", "按门店进行利润计算"),
    PRODUCT("产品维度", "按产品进行利润计算"),
    CATEGORY("类目维度", "按产品类目进行利润计算");

    /**
     * 维度名称
     */
    private final String name;

    /**
     * 维度描述
     */
    private final String description;
} 