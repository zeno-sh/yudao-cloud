package cn.iocoder.yudao.module.chrome.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要订阅验证注解
 * 标记在Controller方法上，表示该方法需要验证用户订阅状态
 *
 * @author Jax
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireSubscription {
    
    /**
     * 是否检查订阅到期
     * @return true表示检查订阅到期，false表示不检查
     */
    boolean checkExpiration() default true;
    
    /**
     * 是否检查积分充足
     * @return true表示检查积分，false表示不检查
     */
    boolean checkCredits() default true;
    
    /**
     * 需要的最小积分数量（当checkCredits=true时生效）
     * @return 最小积分数量，默认为1
     */
    int minCredits() default 1;
    
    /**
     * 功能描述，用于错误提示
     * @return 功能描述
     */
    String feature() default "";
}
