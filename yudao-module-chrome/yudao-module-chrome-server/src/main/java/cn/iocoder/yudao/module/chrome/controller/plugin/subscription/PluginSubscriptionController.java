package cn.iocoder.yudao.module.chrome.controller.plugin.subscription;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.chrome.controller.plugin.subscription.vo.UpgradePriceVO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.chrome.service.subscription.SubscriptionService;
import cn.iocoder.yudao.module.chrome.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.USER_NOT_EXISTS;

/**
 * 插件端 - 订阅管理控制器
 *
 * @author Jax
 */
@Tag(name = "插件端 - 订阅管理")
@RestController
@RequestMapping("/chrome/subscription")
@Validated
@Slf4j
public class PluginSubscriptionController {

    @Resource
    private SubscriptionService subscriptionService;

    @Resource
    private UserService userService;

    @GetMapping("/upgrade-price")
    @Operation(summary = "查询升级价格", description = "计算从当前套餐升级到目标套餐需要支付的差价")
    @Parameters({
            @Parameter(name = "email", description = "用户邮箱", required = true, example = "test@example.com"),
            @Parameter(name = "targetPlanId", description = "目标套餐ID", required = true, example = "4")
    })
    @PermitAll
    public CommonResult<UpgradePriceVO> getUpgradePrice(
            @RequestParam("email") String email,
            @RequestParam("targetPlanId") Long targetPlanId) {
        // 1. 根据邮箱查询用户
        UserDO user = userService.getUserByEmail(email);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        // 2. 计算升级价格
        UpgradePriceVO upgradePriceVO = subscriptionService.calculateUpgradePrice(user.getId(), targetPlanId);

        log.info("[getUpgradePrice][用户({})查询升级价格，目标套餐:{}，结果:{}]",
                email, targetPlanId, upgradePriceVO);

        return success(upgradePriceVO);
    }

    @GetMapping("/check-status")
    @Operation(summary = "校验订阅状态", description = "检查当前登录用户的订阅是否有效")
    public CommonResult<Boolean> checkSubscriptionStatus() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return success(false);
        }
        boolean isValid = subscriptionService.validateSubscriptionStatus(userId);
        return success(isValid);
    }

}
