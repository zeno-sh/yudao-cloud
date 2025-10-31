package cn.iocoder.yudao.module.dm.enums;

import lombok.Getter;

/**
 * 生命周期状态
 *
 * @author: Zeno
 * @createTime: 2024/08/22 15:18
 */
@Getter
public enum PurchaseLifeStatusEnum {

    NOT_ARRIVED("NOT_ARRIVED", "未到货"),
    UNSHIPPED("UNSHIPPED", "已到未发"),
    IN_TRANSIT("IN_TRANSIT", "在途"),
    COMPLETED("COMPLETED", "完成"),
    ;

    private String status;
    private String desc;

    PurchaseLifeStatusEnum(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static PurchaseLifeStatusEnum valueOfStatus(String status) {
        for (PurchaseLifeStatusEnum e : values()) {
            if (e.status.equalsIgnoreCase(status)) {
                return e;
            }
        }
        throw new IllegalArgumentException("No enum constant with status " + status);
    }
}
