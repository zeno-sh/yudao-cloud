package cn.iocoder.yudao.module.dm.enums;

import lombok.Getter;

/**
 * @author: Zeno
 * @createTime: 2024/05/15 11:16
 */
@Getter
public enum FbsPushOrderStatusEnum {

    NOT_PUSHED(0, "未推送"),
    SUCCESS(10, "成功"),
    FAILED(20, "失败"),
    NONE(99, "无需推送"),
    ;
    private Integer status;
    private String description;

    FbsPushOrderStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

}
