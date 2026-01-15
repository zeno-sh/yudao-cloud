package cn.iocoder.yudao.module.chrome.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Chrome 计费周期枚举
 *
 * @author Jax
 */
@AllArgsConstructor
@Getter
public enum BillingCycleEnum {

    /**
     * 月付
     */
    MONTHLY(10, "月付", 30),
    
    /**
     * 年付
     */
    YEARLY(20, "年付", 365),
    /**
     * 一次性购买
     */
    ONE_TIME(30, "一次性购买", -1);

    /**
     * 计费周期代码
     */
    private final Integer code;
    
    /**
     * 计费周期描述
     */
    private final String desc;
    
    /**
     * 有效天数
     */
    private final Integer days;
    
    /**
     * 根据代码获取枚举
     */
    public static BillingCycleEnum valueOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (BillingCycleEnum cycleEnum : values()) {
            if (cycleEnum.getCode().equals(code)) {
                return cycleEnum;
            }
        }
        throw new IllegalArgumentException("未知的计费周期代码: " + code);
    }
}
