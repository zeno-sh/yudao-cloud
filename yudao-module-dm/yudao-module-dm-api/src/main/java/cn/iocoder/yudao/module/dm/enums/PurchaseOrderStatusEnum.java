package cn.iocoder.yudao.module.dm.enums;

import lombok.Getter;

/**
 * @author: Zeno
 * @createTime: 2024/05/11 17:45
 */
@Getter
public enum PurchaseOrderStatusEnum {

    DO_SUBMIT(0, "待提交"),
    DO_ORDER(10, "待下单"),
    DO_ARRIVE(20, "待到货"),
    SUCCESS(30, "已完成"),
    PARTIAL_ARRIVE(40, "部分到货"),
    DO_DELETED(99, "已作废")
    ;


    private Integer status;
    private String desc;

    PurchaseOrderStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
