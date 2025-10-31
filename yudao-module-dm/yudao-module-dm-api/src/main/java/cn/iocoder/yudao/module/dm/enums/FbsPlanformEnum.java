package cn.iocoder.yudao.module.dm.enums;

import lombok.Getter;

/**
 * @author: Zeno
 * @createTime: 2024/08/27 10:43
 */
@Getter
public enum FbsPlanformEnum {
    OZON("OZON"),
    WB("WB"),
    YANDEX("Yandex"),
    ;

    private String name;

    FbsPlanformEnum(String name) {
        this.name = name;
    }

    public static FbsPlanformEnum getByName(String name) {
        for (FbsPlanformEnum value : values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
