package cn.iocoder.yudao.module.chrome.controller.example;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.chrome.framework.annotation.RequireSubscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 订阅验证拦截器使用示例Controller
 * 演示如何使用@RequireSubscription注解
 * 
 * 注意：积分验证和消费请使用 @ConsumeCredits 注解
 *
 * @author Jax
 */
@Tag(name = "订阅验证示例")
@RestController
@RequestMapping("/plugin-api/chrome/example")
@Slf4j
public class SubscriptionExampleController {

    /**
     * 示例1: 检查订阅到期
     */
    @GetMapping("/subscription-check")
    @Operation(summary = "检查订阅到期")
    @RequireSubscription(feature = "订阅状态检查")
    public CommonResult<String> checkSubscription() {
        return CommonResult.success("订阅验证通过，功能可用");
    }

    /**
     * 示例2: 不需要任何验证的接口
     */
    @GetMapping("/no-check")
    @Operation(summary = "无需验证的接口")
    public CommonResult<String> noCheck() {
        return CommonResult.success("无需验证，直接可用");
    }
}

/**
 * 示例3: 在整个Controller类上使用注解
 * 类上的注解会应用到所有方法，除非方法上有自己的注解覆盖
 */
@Tag(name = "类级别订阅验证示例")
@RestController
@RequestMapping("/plugin-api/chrome/class-level")
@RequireSubscription(feature = "类级别验证")
@Slf4j
class ClassLevelSubscriptionController {

    /**
     * 继承类上的注解设置
     */
    @GetMapping("/inherit")
    @Operation(summary = "继承类注解设置")
    public CommonResult<String> inheritClassAnnotation() {
        return CommonResult.success("使用类级别的订阅验证设置");
    }
}
