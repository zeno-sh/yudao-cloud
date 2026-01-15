package cn.iocoder.yudao.module.chrome.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Chrome 支付状态枚举
 *
 * @author Jax
 */
@AllArgsConstructor
@Getter
public enum PaymentStatusEnum {

    /**
     * 待支付
     */
    PENDING(10, "待支付"),
    
    /**
     * 已支付
     */
    PAID(20, "已支付"),
    
    /**
     * 已取消
     */
    CANCELLED(30, "已取消"),
    
    /**
     * 已退款
     */
    REFUNDED(40, "已退款");

    /**
     * 支付状态代码
     */
    private final Integer code;
    
    /**
     * 支付状态描述
     */
    private final String desc;
    
    /**
     * 根据代码获取枚举
     */
    public static PaymentStatusEnum valueOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("未知的支付状态代码: " + code);
    }
}
