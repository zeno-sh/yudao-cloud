package cn.iocoder.yudao.module.platform.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 产品统计排序字段枚举
 * <p>
 * 用于产品统计查询时指定排序字段
 * </p>
 *
 * @author Jax
 */
@Getter
@AllArgsConstructor
public enum ProductOrderByEnum {

    /**
     * 销量
     */
    SALE_NUM("saleNum", "销量"),

    /**
     * 销售额
     */
    SALE_PRICE("salePrice", "销售额"),

    /**
     * 毛利润
     */
    PROFIT("profit", "毛利润"),

    /**
     * 毛利率
     */
    PROFIT_RATE("profitRate", "毛利率"),

    /**
     * 广告花费
     */
    AD_COST("adTotalCost", "广告花费"),

    /**
     * 广告销售额
     */
    AD_SALES("adSalesPrice", "广告销售额"),

    /**
     * ACoS
     */
    ACOS("acos", "ACoS"),

    /**
     * 退款量
     */
    REFUND_NUM("refundNum", "退款量"),

    /**
     * 退款率
     */
    REFUND_RATE("refundRate", "退款率");

    /**
     * 字段编码（用于API传输）
     */
    private final String code;

    /**
     * 字段描述
     */
    private final String description;

    /**
     * JSON 序列化时返回 code
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * JSON 反序列化时根据 code 获取枚举
     */
    @JsonCreator
    public static ProductOrderByEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return SALE_NUM; // 默认按销量排序
        }
        for (ProductOrderByEnum field : values()) {
            if (field.getCode().equalsIgnoreCase(code)) {
                return field;
            }
        }
        return SALE_NUM;
    }

    /**
     * 根据编码获取枚举
     */
    public static ProductOrderByEnum getByCode(String code) {
        return fromCode(code);
    }

}
