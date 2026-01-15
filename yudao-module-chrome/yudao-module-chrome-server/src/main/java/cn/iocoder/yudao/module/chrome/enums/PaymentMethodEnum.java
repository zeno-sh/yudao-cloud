package cn.iocoder.yudao.module.chrome.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Chrome 支付方式枚举
 *
 * @author Jax
 */
@AllArgsConstructor
@Getter
public enum PaymentMethodEnum {

    /**
     * 微信支付
     */
    WECHAT(10, "微信支付"),
    
    /**
     * 支付宝
     */
    ALIPAY(20, "支付宝"),
    
    /**
     * 其他支付方式
     */
    OTHER(30, "其他");

    /**
     * 支付方式代码
     */
    private final Integer code;
    
    /**
     * 支付方式描述
     */
    private final String desc;
    
    /**
     * 根据代码获取枚举
     */
    public static PaymentMethodEnum valueOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentMethodEnum methodEnum : values()) {
            if (methodEnum.getCode().equals(code)) {
                return methodEnum;
            }
        }
        throw new IllegalArgumentException("未知的支付方式代码: " + code);
    }
}
