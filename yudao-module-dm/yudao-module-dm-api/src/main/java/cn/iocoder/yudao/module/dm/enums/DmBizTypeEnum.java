package cn.iocoder.yudao.module.dm.enums;

import lombok.Getter;

/**
 * @author: Zeno
 * @createTime: 2024/05/15 11:16
 */
@Getter
public enum DmBizTypeEnum {
    PURCHASE_ORDER(10, "采购订单"),
    ;
    private Integer type;
    private String name;

    DmBizTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public static String getNameByType(Integer type) {
        for (DmBizTypeEnum value : DmBizTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getName();
            }
        }
        return null;
    }
}
