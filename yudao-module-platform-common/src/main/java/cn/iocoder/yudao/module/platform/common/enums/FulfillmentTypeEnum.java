package cn.iocoder.yudao.module.platform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 履约类型枚举
 * <p>
 * 用于区分订单/库存的履约方式
 * </p>
 *
 * @author Jax
 */
@Getter
@AllArgsConstructor
public enum FulfillmentTypeEnum {

    /**
     * FBA - 平台履约（亚马逊FBA、Coupang火箭配送等）
     */
    FBA(1, "FBA", "平台履约"),

    /**
     * FBM - 卖家自发货
     */
    FBM(2, "FBM", "卖家自发货");

    /**
     * 类型编码
     */
    private final Integer code;

    /**
     * 类型标识
     */
    private final String type;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     */
    public static FulfillmentTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FulfillmentTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据亚马逊履约渠道转换
     * MFN -> FBM, AFN -> FBA
     */
    public static FulfillmentTypeEnum fromAmazonChannel(String fulfillmentChannel) {
        if ("AFN".equalsIgnoreCase(fulfillmentChannel)) {
            return FBA;
        }
        return FBM;
    }

}
