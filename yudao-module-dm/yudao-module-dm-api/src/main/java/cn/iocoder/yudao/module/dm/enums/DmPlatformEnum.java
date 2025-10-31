package cn.iocoder.yudao.module.dm.enums;

import lombok.Getter;

/**
 * @author: Zeno
 * @createTime: 2024/07/13 00:39
 */
@Getter
public enum DmPlatformEnum {
    OZON(10, "OZON-本土"),
    OZON_GLOBAL(20, "OZON-跨境"),
    WB(30, "Wildberries-本土"),
    WB_GLOBAL(40, "Wildberries-跨境"),
    YANDEX(50, "Yandex-本土"),
    YANDEX_GLOBAL(60, "Yandex-跨境"),
    ;

    private Integer platformId;
    private String platformName;

    DmPlatformEnum(Integer platformId, String platformName) {
        this.platformId = platformId;
        this.platformName = platformName;
    }
}
