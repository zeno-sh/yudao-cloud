package cn.iocoder.yudao.module.chrome.service.payment;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.chrome.controller.plugin.payment.vo.ChromePaymentCreateReqVO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.order.SubscriptionOrderMapper;
import cn.iocoder.yudao.module.chrome.enums.PaymentMethodEnum;
import cn.iocoder.yudao.module.chrome.enums.PaymentStatusEnum;
import cn.iocoder.yudao.module.chrome.service.plan.SubscriptionPlanService;
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

        // 1.2 计算实际支付金额（分）
        BigDecimal actualPrice = plan.getDiscountedPrice() != null ? plan.getDiscountedPrice() : plan.getPrice();
        Integer payPrice = actualPrice.multiply(BigDecimal.valueOf(100)).intValue(); // 转换为分

        // 1.3 生成订单号
        String orderNo = generateOrderNo(userId);

        // 2.1 创建订阅订单
        SubscriptionOrderDO order = SubscriptionOrderDO.builder()
                .orderNo(orderNo)
                .userId(userId)
                .planId(plan.getId())
                .subscriptionType(plan.getSubscriptionType())
                .billingCycle(plan.getBillingCycle())
                .credits(plan.getCredits())
                .originalPrice(plan.getPrice())
                .actualPrice(actualPrice)
                .currency(plan.getCurrency())
                .paymentMethod(PaymentMethodEnum.ALIPAY.getCode()) // 主要使用支付宝
                .paymentStatus(PaymentStatusEnum.PENDING.getCode()) // 待支付
                .expireTime(addTime(Duration.ofHours(2L))) // 2小时后过期
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
                .setBody(buildOrderBody(plan)) // 订单描述
                .setPrice(payPrice) // 支付金额（分）
                .setExpireTime(order.getExpireTime())).getData(); // 过期时间

        // 2.3 更新支付单ID到订单
        subscriptionOrderMapper.updateById(SubscriptionOrderDO.builder()
                .id(order.getId())
                .payOrderId(payOrderId)
                .build());

        log.info("[createPaymentOrder][用户({}) 创建支付订单成功，订单号:{}，支付单ID:{}]", 
                userId, orderNo, payOrderId);
        
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
        // 注意：这里会调用SubscriptionOrderService内部处理订单支付的逻辑
        // 通过重新设置订单的完整信息，让processOrderPayment方法能够正确处理
        SubscriptionOrderDO completeOrder = SubscriptionOrderDO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .subscriptionType(order.getSubscriptionType())
                .billingCycle(order.getBillingCycle())
                .planId(order.getPlanId())
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
                subscriptionService.upgradeSubscription(order.getUserId(), 
                        order.getSubscriptionType(), 
                        paymentDuration, 
                        order.getPlanId());
            }
        }
    }
    
    /**
     * 根据计费周期计算付费时长
     *
     * @param billingCycle 计费周期
     * @param plan 套餐信息
     * @return 付费时长（天数）
     */
    private Integer calculatePaymentDuration(Integer billingCycle, SubscriptionPlanDO plan) {
        if (cn.iocoder.yudao.module.chrome.enums.BillingCycleEnum.MONTHLY.getCode().equals(billingCycle)) {
            return 30; // 月付：30天
        } else if (cn.iocoder.yudao.module.chrome.enums.BillingCycleEnum.YEARLY.getCode().equals(billingCycle)) {
            return 365; // 年付：365天
        } else if (cn.iocoder.yudao.module.chrome.enums.BillingCycleEnum.ONE_TIME.getCode().equals(billingCycle)) {
            return 0; // 一次性购买（积分包）：0天
        }
        return 30; // 默认30天
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
     * @param order 订阅订单
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
        return plan.getPlanName() + " - 积分充值";
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
}
