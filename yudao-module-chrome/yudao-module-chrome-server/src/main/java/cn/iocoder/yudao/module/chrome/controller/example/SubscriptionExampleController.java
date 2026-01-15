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
 * @author Jax
 */
@Tag(name = "订阅验证示例")
@RestController
@RequestMapping("/plugin-api/chrome/example")
@Slf4j
public class SubscriptionExampleController {

    /**
     * 示例1: 只检查订阅到期，不检查积分
     */
    @GetMapping("/subscription-only")
    @Operation(summary = "只检查订阅到期")
    @RequireSubscription(checkExpiration = true, checkCredits = false, feature = "订阅状态检查")
    public CommonResult<String> checkSubscriptionOnly() {
        return CommonResult.success("订阅验证通过，功能可用");
    }

    /**
     * 示例2: 只检查积分，不检查订阅到期
     */
    @GetMapping("/credits-only")
    @Operation(summary = "只检查积分充足")
    @RequireSubscription(checkExpiration = false, checkCredits = true, minCredits = 10, feature = "积分检查")
    public CommonResult<String> checkCreditsOnly() {
        return CommonResult.success("积分充足，功能可用");
    }

    /**
     * 示例3: 同时检查订阅和积分
     */
    @PostMapping("/full-check")
    @Operation(summary = "同时检查订阅和积分")
    @RequireSubscription(checkExpiration = true, checkCredits = true, minCredits = 5, feature = "完整验证")
    public CommonResult<String> fullCheck(@RequestBody String data) {
        log.info("处理数据: {}", data);
        return CommonResult.success("订阅和积分验证都通过，功能可用");
    }

    /**
     * 示例4: 不需要任何验证的接口
     */
    @GetMapping("/no-check")
    @Operation(summary = "无需验证的接口")
    public CommonResult<String> noCheck() {
        return CommonResult.success("无需验证，直接可用");
    }
}

/**
 * 示例5: 在整个Controller类上使用注解
 * 类上的注解会应用到所有方法，除非方法上有自己的注解覆盖
 */
@Tag(name = "类级别订阅验证示例")
@RestController
@RequestMapping("/plugin-api/chrome/class-level")
@RequireSubscription(checkExpiration = true, checkCredits = true, minCredits = 3, feature = "类级别验证")
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

    /**
     * 方法级别的注解会覆盖类级别的注解
     */
    @GetMapping("/override")
    @Operation(summary = "覆盖类注解设置")
    @RequireSubscription(checkExpiration = false, checkCredits = true, minCredits = 1, feature = "方法级覆盖")
    public CommonResult<String> overrideClassAnnotation() {
        return CommonResult.success("使用方法级别的订阅验证设置");
    }
}
