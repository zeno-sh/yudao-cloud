package cn.iocoder.yudao.module.chrome.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Chrome 积分交易类型枚举
 *
 * @author Jax
 */
@AllArgsConstructor
@Getter
public enum TransactionTypeEnum {

    /**
     * 充值
     */
    RECHARGE(10, "充值"),
    
    /**
     * 消费
     */
    CONSUME(20, "消费"),
    
    /**
     * 赠送
     */
    GIFT(30, "赠送"),
    
    /**
     * 重置
     */
    RESET(40, "重置"),
    
    /**
     * API调用记录（方法执行失败，未消费积分）
     */
    API_CALL_FAILED(50, "API调用失败"),
    
    /**
     * API调用记录（返回值无效，未消费积分）
     */
    API_CALL_NO_DATA(60, "API调用无数据");

    /**
     * 交易类型代码
     */
    private final Integer code;
    
    /**
     * 交易类型描述
     */
    private final String desc;
    
    /**
     * 根据代码获取枚举
     */
    public static TransactionTypeEnum valueOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (TransactionTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        throw new IllegalArgumentException("未知的交易类型代码: " + code);
    }
}
