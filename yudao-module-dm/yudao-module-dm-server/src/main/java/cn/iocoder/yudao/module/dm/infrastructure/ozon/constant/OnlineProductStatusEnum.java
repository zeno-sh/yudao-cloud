package cn.iocoder.yudao.module.dm.infrastructure.ozon.constant;

import lombok.Getter;

/**
 * @author: Zeno
 * @createTime: 2025/03/31 21:13
 */
@Getter
public enum OnlineProductStatusEnum {
    ONLINE(10, "Продается", "正常销售"),
    READY(20, "Готов к продаже", "准备出售"),
    OFFLINE(30, "Не продается", "已下架"),
    ERRORS(40, "Не продается", "有错误"),
    ARCHIVED(50, "Не продается", "已归档"),
    ;

    private Integer code;

    private String name;

    private String desc;

    OnlineProductStatusEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static OnlineProductStatusEnum get(String name) {
        for (OnlineProductStatusEnum value : OnlineProductStatusEnum.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
