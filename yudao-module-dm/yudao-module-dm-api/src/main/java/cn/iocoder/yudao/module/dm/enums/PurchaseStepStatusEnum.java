package cn.iocoder.yudao.module.dm.enums;

import lombok.Getter;

/**
 * @author: Zeno
 * @createTime: 2024/06/04 15:42
 */
@Getter
public enum PurchaseStepStatusEnum {
    WAIT("wait", "等待"),
    SUCCESS("success", "成功"),
    FINISH("finish", "结束"),
    PROCESS("process", "进行中"),
    ERROR("error", "失败"),
    ;

    private String status;
    private String desc;

    PurchaseStepStatusEnum(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
