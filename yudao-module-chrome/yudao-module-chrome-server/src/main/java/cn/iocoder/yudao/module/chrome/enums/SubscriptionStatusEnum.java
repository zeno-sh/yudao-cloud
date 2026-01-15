package cn.iocoder.yudao.module.chrome.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Chrome 订阅状态枚举
 *
 * @author Jax
 */
@AllArgsConstructor
@Getter
public enum SubscriptionStatusEnum {

    /**
     * 有效状态
     */
    ACTIVE(1, "有效"),
    
    /**
     * 无效状态
     */
    INACTIVE(0, "无效"),
    
    /**
     * 已过期
     */
    EXPIRED(-1, "已过期");

    /**
     * 状态值
     */
    private final Integer status;
    
    /**
     * 状态名称
     */
    private final String name;

}