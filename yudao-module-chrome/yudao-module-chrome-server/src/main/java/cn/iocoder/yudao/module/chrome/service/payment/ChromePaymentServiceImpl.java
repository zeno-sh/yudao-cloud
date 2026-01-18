package cn.iocoder.yudao.module.chrome.service.payment;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo.ChromePaymentCreateReqVO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.order.SubscriptionOrderMapper;
import cn.iocoder.yudao.module.chrome.enums.BillingCycleEnum;
import cn.iocoder.yudao.module.chrome.enums.PaymentMethodEnum;
import cn.iocoder.yudao.module.chrome.enums.PaymentStatusEnum;
import cn.iocoder.yudao.module.chrome.enums.SubscriptionTypeEnum;
import cn.iocoder.yudao.module.chrome.service.plan.SubscriptionPlanService;
import cn.iocoder.yudao.module.chrome.service.referral.ReferralService;
import cn.iocoder.yudao.module.chrome.service.subscription.SubscriptionService;
import cn.iocoder.yudao.module.chrome.service.user.UserService;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import org.springframework.context.annotation.Lazy;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * Chrome 支付服务实现类
 *
 * @author Jax
 */
@Service
@Validated
@Slf4j
public class ChromePaymentServiceImpl implements ChromePaymentService {

    /**
     * Chrome插件支付应用标识
     * 需要在 [支付管理 -> 应用信息] 里添加
     */
    private static final String PAY_APP_KEY = "chrome";

    @Resource
    private PayOrderApi payOrderApi;

    @Resource
    private SubscriptionPlanService subscriptionPlanService;

    @Resource
    private SubscriptionOrderMapper subscriptionOrderMapper;

    @Resource
    private SubscriptionService subscriptionService;

    @Resource
    private UserService userService;

    @Resource
    @Lazy // 避免循环依赖
    private ReferralService referralService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPaymentOrder(ChromePaymentCreateReqVO createReqVO) {
        // 0. 根据邮箱查询用户
        UserDO user = userService.getUserByEmail(createReqVO.getEmail());
        Assert.notNull(user, "用户不存在，邮箱: {}", createReqVO.getEmail());
        Long userId = user.getId();
        // 1.1 校验并获取套餐信息
        SubscriptionPlanDO plan = subscriptionPlanService.getSubscriptionPlan(createReqVO.getPlanId());
        Assert.notNull(plan, "套餐({})不存在", createReqVO.getPlanId());
        Assert.isTrue(plan.getStatus(), "套餐({})已禁用", createReqVO.getPlanId());

        // 1.2 计算升级价格（差价）
        cn.iocoder.yudao.module.chrome.controller.plugin.subscription.vo.UpgradePriceVO upgradePriceVO = subscriptionService
                .calculateUpgradePrice(userId, createReqVO.getPlanId());

        // 1.3 校验是否允许购买（降级场景禁止）
        if (!Boolean.TRUE.equals(upgradePriceVO.getIsUpgrade())) {
            log.warn("[createPaymentOrder][用户({})尝试降级购买套餐({})，被禁止: {}]",
                    userId, createReqVO.getPlanId(), upgradePriceVO.getMessage());
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception(
                    cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.SUBSCRIPTION_DOWNGRADE_NOT_ALLOWED);
        }

        // 1.4 获取实际支付金额（元 -> 分）
        BigDecimal actualPrice = upgradePriceVO.getUpgradePrice();
        Integer payPrice = actualPrice.multiply(BigDecimal.valueOf(100)).intValue();

        // 1.5 如果差价为0，直接处理订阅（免费升级）
        if (payPrice <= 0) {
            log.info("[createPaymentOrder][用户({})免费升级套餐({})]", userId, createReqVO.getPlanId());
            // 直接调用订阅升级
            Integer paymentDuration = calculatePaymentDuration(plan.getBillingCycle(), plan);
            subscriptionService.upgradeSubscription(userId, plan.getSubscriptionType(), paymentDuration, plan.getId());
            return -1L; // 返回特殊值表示免费升级，无需支付
        }

        // 1.6 生成订单号
        String orderNo = generateOrderNo(userId);

        // 2.1 创建订阅订单（记录原价和实际支付价格）
        // 根据订单类型设置备注
        String orderRemark = Boolean.TRUE.equals(upgradePriceVO.getIsRenewal()) ? "续费"
                : (upgradePriceVO.getRemainingValue() != null &&
                        upgradePriceVO.getRemainingValue().compareTo(java.math.BigDecimal.ZERO) > 0) ? "升级" : "首次订阅";

        SubscriptionOrderDO order = SubscriptionOrderDO.builder()
                .orderNo(orderNo)
                .userId(userId)
                .planId(plan.getId())
                .subscriptionType(plan.getSubscriptionType())
                .billingCycle(plan.getBillingCycle())
                .credits(plan.getCredits())
                .originalPrice(upgradePriceVO.getOriginalPrice()) // 目标套餐原价
                .actualPrice(actualPrice) // 实际支付差价
                .currency(plan.getCurrency())
                .paymentMethod(PaymentMethodEnum.ALIPAY.getCode()) // 主要使用支付宝
                .paymentStatus(PaymentStatusEnum.PENDING.getCode()) // 待支付
                .expireTime(addTime(Duration.ofHours(2L))) // 2小时后过期（支付超时时间）
                .durationDays(plan.getDurationDays()) // 套餐时长
                .remark(orderRemark) // 订单备注
                .build();
        subscriptionOrderMapper.insert(order);

        // 2.2 创建支付单（使用订单号作为商户订单编号）
        Long payOrderId = payOrderApi.createOrder(new PayOrderCreateReqDTO()
                .setAppKey(PAY_APP_KEY)
                .setUserIp(getClientIP()) // 用户IP
                .setUserId(userId)
                .setUserType(UserTypeEnum.PLUGIN.getValue()) // 用户类型
                .setMerchantOrderId(orderNo) // 业务订单编号（使用订单号）
                .setSubject(buildOrderSubject(plan)) // 订单标题
                .setBody(buildUpgradeOrderBody(plan, upgradePriceVO)) // 订单描述（含差价信息）
                .setPrice(payPrice) // 支付金额（分）
                .setExpireTime(order.getExpireTime())).getData(); // 过期时间

        // 2.3 更新支付单ID到订单
        subscriptionOrderMapper.updateById(SubscriptionOrderDO.builder()
                .id(order.getId())
                .payOrderId(payOrderId)
                .build());

        log.info("[createPaymentOrder][用户({}) 创建支付订单成功，订单号:{}，支付单ID:{}，原价:{}，差价:{}]",
                userId, orderNo, payOrderId, upgradePriceVO.getOriginalPrice(), actualPrice);

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderPaid(String orderNo, Long payOrderId) {
        // 1.1 根据订单号查询订单
        SubscriptionOrderDO order = subscriptionOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.error("[updateOrderPaid][订单号({}) 不存在，支付单ID({})]", orderNo, payOrderId);
            throw exception(SUBSCRIPTION_ORDER_NOT_EXISTS);
        }

        // 1.2 校验订单是否已支付
        if (PaymentStatusEnum.PAID.getCode().equals(order.getPaymentStatus())) {
            log.warn("[updateOrderPaid][订单号({}) 已支付，直接返回]", orderNo);
            return;
        }

        // 2. 校验支付订单的合法性
        PayOrderRespDTO payOrder = validatePayOrderPaid(order, payOrderId);

        // 3. 更新订单状态为已支付
        SubscriptionOrderDO updateOrder = SubscriptionOrderDO.builder()
                .id(order.getId())
                .paymentStatus(PaymentStatusEnum.PAID.getCode())
                .paymentTime(LocalDateTime.now())
                .build();
        subscriptionOrderMapper.updateById(updateOrder);

        // 4. 处理订单支付后的业务逻辑（订阅升级、积分充值等）
        // 注意：需要传递完整的订单信息，包括actualPrice用于佣金计算
        SubscriptionOrderDO completeOrder = SubscriptionOrderDO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .subscriptionType(order.getSubscriptionType())
                .billingCycle(order.getBillingCycle())
                .planId(order.getPlanId())
                .actualPrice(order.getActualPrice()) // 用于推荐佣金计算
                .build();

        // 直接调用订阅服务处理升级逻辑
        processPaymentSuccess(completeOrder);

        log.info("[updateOrderPaid][订单号({}) 支付成功，支付单ID({})]", orderNo, payOrderId);
    }

    /**
     * 处理支付成功后的业务逻辑
     *
     * @param order 订单信息
     */
    private void processPaymentSuccess(SubscriptionOrderDO order) {
        // 处理用户订阅信息（升级/续费）
        if (order.getSubscriptionType() != null && order.getBillingCycle() != null) {
            // 获取套餐信息以确定订阅时长
            SubscriptionPlanDO plan = subscriptionPlanService.getSubscriptionPlan(order.getPlanId());
            if (plan != null) {
                // 计算订阅时长（天数）
                Integer paymentDuration = calculatePaymentDuration(order.getBillingCycle(), plan);

                // 智能升级或续费订阅（会自动处理升级规则和积分充值）
                Long subscriptionId = subscriptionService.upgradeSubscription(order.getUserId(),
                        order.getSubscriptionType(),
                        paymentDuration,
                        order.getPlanId());

                // 更新订单的过期时间（记录本次充值对应的订阅结束时间）
                SubscriptionDO subscription = subscriptionService.getSubscription(subscriptionId);
                if (subscription != null) {
                    SubscriptionOrderDO updateOrder = SubscriptionOrderDO.builder()
                            .id(order.getId())
                            .expireTime(subscription.getEndTime())
                            .build();
                    subscriptionOrderMapper.updateById(updateOrder);
                }

                // 异步触发推广奖励逻辑（处理推荐人佣金和被推荐人15天赠送）
                referralService.processPaySuccessAsync(order);
            }
        }
    }

    /**
     * 根据计费周期计算付费时长
     *
     * @param billingCycle 计费周期
     * @param plan         套餐信息
     * @return 付费时长（天数）
     */
    private Integer calculatePaymentDuration(Integer billingCycle, SubscriptionPlanDO plan) {
        // 优先使用套餐配置的订阅时长
        if (plan != null && plan.getDurationDays() != null && plan.getDurationDays() > 0) {
            return plan.getDurationDays();
        }

        // 一次性购买（积分包）：0天，表示不基于时间，永久有效
        if (cn.iocoder.yudao.module.chrome.enums.BillingCycleEnum.ONE_TIME.getCode().equals(billingCycle)) {
            return 0;
        }

        // 兜底默认值，避免套餐配置缺失时出错
        log.warn("[calculatePaymentDuration][套餐({})未配置订阅时长，使用默认值30天]",
                plan != null ? plan.getId() : null);
        return 30;
    }

    @Override
    public void cancelOrder(Long orderId) {
        // 1. 校验订单是否存在
        SubscriptionOrderDO order = subscriptionOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(SUBSCRIPTION_ORDER_NOT_EXISTS);
        }

        // 2. 校验订单状态是否可以取消
        if (!PaymentStatusEnum.PENDING.getCode().equals(order.getPaymentStatus())) {
            log.warn("[cancelOrder][订单({}) 不是待支付状态，无法取消]", orderId);
            return;
        }

        // 3. 更新订单状态为已取消
        subscriptionOrderMapper.updateById(SubscriptionOrderDO.builder()
                .id(orderId)
                .paymentStatus(PaymentStatusEnum.CANCELLED.getCode())
                .build());

        log.info("[cancelOrder][订单({}) 已取消]", orderId);
    }

    /**
     * 校验支付订单的合法性
     *
     * @param order      订阅订单
     * @param payOrderId 支付订单编号
     * @return 支付订单
     */
    private PayOrderRespDTO validatePayOrderPaid(SubscriptionOrderDO order, Long payOrderId) {
        // 1. 校验支付单是否存在
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId).getData();
        if (payOrder == null) {
            log.error("[validatePayOrderPaid][订单({}) 支付单({}) 不存在]", order.getId(), payOrderId);
            throw exception(SUBSCRIPTION_ORDER_NOT_EXISTS);
        }

        // 2.1 校验支付单已支付
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validatePayOrderPaid][订单({}) 支付单({}) 未支付，payOrder数据：{}]",
                    order.getId(), payOrderId, toJsonString(payOrder));
            throw exception(SUBSCRIPTION_ORDER_NOT_EXISTS);
        }

        // 2.2 校验支付金额一致
        Integer orderPrice = order.getActualPrice().multiply(BigDecimal.valueOf(100)).intValue();
        if (!ObjectUtil.equals(payOrder.getPrice(), orderPrice)) {
            log.error("[validatePayOrderPaid][订单({}) 支付单({}) 支付金额不匹配，订单金额:{}，支付金额:{}]",
                    order.getId(), payOrderId, orderPrice, payOrder.getPrice());
            throw exception(SUBSCRIPTION_ORDER_NOT_EXISTS);
        }

        // 2.3 校验支付订单匹配（商户订单号应该等于订单号）
        if (!ObjectUtil.equals(payOrder.getMerchantOrderId(), order.getOrderNo())) {
            log.error("[validatePayOrderPaid][订单号({}) 支付单不匹配({})，payOrder数据：{}]",
                    order.getOrderNo(), payOrderId, toJsonString(payOrder));
            throw exception(SUBSCRIPTION_ORDER_NOT_EXISTS);
        }

        return payOrder;
    }

    /**
     * 生成订单号
     *
     * @param userId 用户ID
     * @return 订单号
     */
    private String generateOrderNo(Long userId) {
        // 格式：CHR + 时间戳 + 用户ID后4位
        String timestamp = String.valueOf(System.currentTimeMillis());
        String userIdSuffix = StrUtil.padPre(String.valueOf(userId % 10000), 4, '0');
        return "CHR" + timestamp + userIdSuffix;
    }

    /**
     * 构建订单标题
     *
     * @param plan 套餐信息
     * @return 订单标题
     */
    private String buildOrderSubject(SubscriptionPlanDO plan) {
        return plan.getPlanName() + String.format("- %s",
                plan.getSubscriptionType().equals(SubscriptionTypeEnum.CREDITS_PACK.getCode()) ? "积分充值" : "套餐订阅");
    }

    /**
     * 构建订单描述
     *
     * @param plan 套餐信息
     * @return 订单描述
     */
    private String buildOrderBody(SubscriptionPlanDO plan) {
        return "购买" + plan.getCredits() + "积分";
    }

    /**
     * 构建升级订单描述（含差价信息）
     *
     * @param plan           套餐信息
     * @param upgradePriceVO 升级价格信息
     * @return 订单描述
     */
    private String buildUpgradeOrderBody(SubscriptionPlanDO plan,
            cn.iocoder.yudao.module.chrome.controller.plugin.subscription.vo.UpgradePriceVO upgradePriceVO) {
        if (Boolean.TRUE.equals(upgradePriceVO.getIsRenewal())) {
            return "续费" + plan.getPlanName();
        }
        if (upgradePriceVO.getRemainingValue() != null
                && upgradePriceVO.getRemainingValue().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return "升级至" + plan.getPlanName() + "（差价支付）";
        }
        return "购买" + plan.getPlanName();
    }
}
