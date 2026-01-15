package cn.iocoder.yudao.module.chrome.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要订阅验证注解
 * 标记在Controller方法或类上，表示该方法需要验证用户订阅状态（时长）
 * 
 * 注意：积分验证和消费请使用 @ConsumeCredits 注解
 *
 * @author Jax
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireSubscription {
    
    /**
     * 功能描述，用于错误提示
     * @return 功能描述
     */
    String feature() default "";
}
