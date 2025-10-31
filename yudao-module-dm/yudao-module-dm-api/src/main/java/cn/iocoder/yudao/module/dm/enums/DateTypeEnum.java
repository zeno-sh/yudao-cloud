package cn.iocoder.yudao.module.dm.enums;

import lombok.Getter;

/**
 * @author: Zeno
 * @createTime: 2024/08/05 16:25
 */
@Getter
public enum DateTypeEnum {
    DATE_TYPE_1(1,"日"),
    DATE_TYPE_2(2,"周"),
    DATE_TYPE_3(3,"月"),
    DATE_TYPE_4(4,"季"),
    DATE_TYPE_5(5,"年")
    ;

    private Integer type;
    private String desc;

    DateTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static DateTypeEnum valueOf(Integer type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        for (DateTypeEnum dateTypeEnum : values()) {
            if (dateTypeEnum.getType().equals(type)) {
                return dateTypeEnum;
            }
        }
        throw new IllegalArgumentException("No matching enum type for value: " + type);
    }
}
