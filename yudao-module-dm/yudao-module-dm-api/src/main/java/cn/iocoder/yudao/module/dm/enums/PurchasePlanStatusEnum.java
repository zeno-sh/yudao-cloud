package cn.iocoder.yudao.module.dm.enums;

import lombok.Getter;

/**
 * @author: Zeno
 * @createTime: 2024/05/11 17:45
 */
@Getter
public enum PurchasePlanStatusEnum {

    DO_PURCHASED(0, "待采购"),
    SUCCESS(10, "已完成"),
    REJECTED(20, "已驳回"),
    DO_DELETED(30, "已作废")
    ;


    private Integer status;
    private String desc;

    PurchasePlanStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
