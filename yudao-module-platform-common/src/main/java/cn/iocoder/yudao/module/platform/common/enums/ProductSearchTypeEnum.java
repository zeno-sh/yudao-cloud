package cn.iocoder.yudao.module.platform.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 产品搜索类型枚举
 * <p>
 * 用于产品统计查询时指定搜索维度
 * </p>
 *
 * @author Jax
 */
@Getter
@AllArgsConstructor
public enum ProductSearchTypeEnum {

    /**
     * ASIN - 子ASIN搜索
     */
    ASIN("asin", "ASIN"),

    /**
     * 父ASIN - 父体ASIN搜索
     */
    PARENT_ASIN("parentAsin", "父ASIN"),

    /**
     * MSKU - 商家SKU搜索
     */
    MSKU("msku", "MSKU"),

    /**
     * SKU - 本地SKU搜索
     */
    SKU("sku", "本地SKU");

    /**
     * 类型编码（用于API传输）
     */
    private final String code;

    /**
     * 类型描述
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
    public static ProductSearchTypeEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (ProductSearchTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据编码获取枚举
     */
    public static ProductSearchTypeEnum getByCode(String code) {
        return fromCode(code);
    }

}
