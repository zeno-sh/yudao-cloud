package cn.iocoder.yudao.module.dm.enums;

import lombok.Getter;

/**
 * @author: Zeno
 * @createTime: 2025/04/11 11:33
 */
@Getter
public enum VolumeQueryTypeEnum {

    SHOP_TYPE("shop", "店铺"),
    OFFER_ID_TYPE("offerId", "货号"),
    SKU_TYPE("sku", "SKU");

    private String type;
    private String desc;

    VolumeQueryTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

}
