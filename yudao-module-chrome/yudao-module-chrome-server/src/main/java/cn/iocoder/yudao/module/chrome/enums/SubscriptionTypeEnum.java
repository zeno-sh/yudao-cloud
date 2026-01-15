package cn.iocoder.yudao.module.chrome.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Chrome 订阅类型枚举
 *
 * @author Jax
 */
@AllArgsConstructor
@Getter
public enum SubscriptionTypeEnum {

    /**
     * 免费版
     */
    FREE(10, "FREE", "免费版"),
    
    /**
     * 基础版
     */
    BASIC(20, "BASIC", "基础版"),
    
    /**
     * 高级版
     */
    PREMIUM(30, "PREMIUM", "高级版"),
    
    /**
     * 积分包
     */
    CREDITS_PACK(40, "CREDITS_PACK", "积分包");

    /**
     * 订阅类型代码
     */
    private final Integer code;
    
    /**
     * 订阅类型
     */
    private final String type;
    
    /**
     * 订阅名称
     */
    private final String name;
    
    /**
     * 根据代码获取枚举
     */
    public static SubscriptionTypeEnum valueOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (SubscriptionTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        throw new IllegalArgumentException("未知的订阅类型代码: " + code);
    }

}