package cn.iocoder.yudao.module.chrome.framework.annotation;

import cn.iocoder.yudao.module.chrome.enums.FeatureTypeEnum;

import java.lang.annotation.*;

/**
 * 积分消费注解
 * 用于标记需要消费积分的方法，通过AOP统一处理积分消费和记录
 *
 * @author Jax
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConsumeCredits {

    /**
     * 功能类型
     */
    FeatureTypeEnum featureType();

    /**
     * 消费积分数量，默认1
     */
    int credits() default 1;

    /**
     * 业务描述，可选
     */
    String description() default "";

    /**
     * 是否在方法执行前消费积分，默认true
     * true: 方法执行前消费积分，失败则不执行方法
     * false: 方法执行成功后消费积分
     */
    boolean consumeBeforeExecution() default true;

    /**
     * 是否需要检查返回结果有效性，默认false
     * true: 只有当返回结果不为null且包含有效数据时才消费积分
     * false: 只要方法执行成功就消费积分
     */
    boolean checkReturnValue() default false;
}
