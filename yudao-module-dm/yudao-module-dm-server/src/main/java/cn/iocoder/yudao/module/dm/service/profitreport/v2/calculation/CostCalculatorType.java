package cn.iocoder.yudao.module.dm.service.profitreport.v2.calculation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成本计算器类型
 *
 * @author Jax
 */
@Getter
@AllArgsConstructor
public enum CostCalculatorType {
    
    /**
     * 签收订单成本计算器
     */
    SIGNED_ORDER("signed_order", "签收订单成本计算"),
    
    /**
     * 退货成本计算器
     */
    RETURN_ORDER("return_order", "退货成本计算"),
    
    /**
     * 收单费用计算器
     */
    ORDER_FEE("order_fee", "收单费用计算"),
    
    /**
     * 利润计算器
     */
    PROFIT("profit", "利润计算");
    
    /**
     * 类型代码
     */
    private final String code;
    
    /**
     * 类型名称
     */
    private final String name;
} 