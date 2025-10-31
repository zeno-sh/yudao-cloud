package cn.iocoder.yudao.module.dm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 门店授权状态枚举
 * 
 * @author Jax
 */
@Getter
@AllArgsConstructor
public enum AuthStatusEnum {

    /**
     * 正常
     */
    NORMAL(10, "正常"),
    
    /**
     * 已过期
     */
    EXPIRED(20, "已过期"),
    
    /**
     * 已禁用
     */
    DISABLED(30, "已禁用"),
    
    /**
     * 待审核
     */
    PENDING(40, "待审核");

    /**
     * 状态值
     */
    private final Integer status;
    
    /**
     * 状态名称
     */
    private final String name;

    /**
     * 根据状态值获取枚举
     */
    public static AuthStatusEnum getByStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (AuthStatusEnum authStatus : values()) {
            if (authStatus.getStatus().equals(status)) {
                return authStatus;
            }
        }
        return null;
    }

    /**
     * 判断是否为正常状态
     */
    public static boolean isNormal(Integer status) {
        return NORMAL.getStatus().equals(status);
    }

    /**
     * 判断是否为过期状态
     */
    public static boolean isExpired(Integer status) {
        return EXPIRED.getStatus().equals(status);
    }

    /**
     * 判断是否为禁用状态
     */
    public static boolean isDisabled(Integer status) {
        return DISABLED.getStatus().equals(status);
    }

    /**
     * 判断是否为待审核状态
     */
    public static boolean isPending(Integer status) {
        return PENDING.getStatus().equals(status);
    }
} 