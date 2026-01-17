package cn.iocoder.yudao.module.chrome.service.order;

import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.service.referral.ReferralService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.chrome.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.order.SubscriptionOrderDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.chrome.dal.mysql.order.SubscriptionOrderMapper;
import cn.iocoder.yudao.module.chrome.service.credits.UserCreditsService;
import cn.iocoder.yudao.module.chrome.service.subscription.SubscriptionService;
import cn.iocoder.yudao.module.chrome.service.plan.SubscriptionPlanService;
import cn.iocoder.yudao.module.chrome.enums.BillingCycleEnum;
import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * 订阅订单 Service 实现类
 *
 * @author Jax
 */
@Service
@Validated
@Slf4j
public class SubscriptionOrderServiceImpl implements SubscriptionOrderService {

    @Resource
    private SubscriptionOrderMapper subscriptionOrderMapper;

    @Resource
    private UserCreditsService userCreditsService;

    @Resource
    private SubscriptionService subscriptionService;

    @Resource
    private SubscriptionPlanService subscriptionPlanService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSubscriptionOrder(SubscriptionOrderSaveReqVO createReqVO) {
        // 插入订单
        SubscriptionOrderDO subscriptionOrder = BeanUtils.toBean(createReqVO, SubscriptionOrderDO.class);
        subscriptionOrderMapper.insert(subscriptionOrder);

        // 如果支付状态为已支付，处理后续业务逻辑
        if (createReqVO.getPaymentStatus() != null && createReqVO.getPaymentStatus() == 20) {
            processOrderPayment(subscriptionOrder);
        }

        // 返回
        return subscriptionOrder.getId();
    }

    /**
     * 处理订单支付后的业务逻辑
     * 
     * @param order 订单信息
     */
    private void processOrderPayment(SubscriptionOrderDO order) {
        // 处理用户订阅信息（升级/续费）
        // 注意：积分充值逻辑已移至订阅服务中，避免重复充值
        if (order.getSubscriptionType() != null && order.getBillingCycle() != null) {
            // 获取套餐信息以确定订阅时长
            SubscriptionPlanDO plan = subscriptionPlanService.getSubscriptionPlan(order.getPlanId());
            if (plan != null) {
                // 计算订阅时长（天数）
                Integer paymentDuration = calculatePaymentDuration(order.getBillingCycle(), plan);

                // 智能升级或续费订阅（会自动处理升级规则和积分充值）
                Long subscriptionId = subscriptionService.upgradeSubscription(order.getUserId(),
                        order.getSubscriptionType(), paymentDuration,
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

                // [NEW] 触发推广奖励逻辑 (异步)
                referralService.processPaySuccessAsync(order);
            }
        }
    }

    /**
     * 根据套餐配置获取付费时长
     * 
     * @param billingCycle 计费周期 (10=月付, 20=年付, 30=一次性购买)
     * @param plan         套餐信息
     * @return 付费时长（天数）
     */
    private Integer calculatePaymentDuration(Integer billingCycle, SubscriptionPlanDO plan) {
        // 优先使用套餐配置的订阅时长
        if (plan != null && plan.getDurationDays() != null && plan.getDurationDays() > 0) {
            return plan.getDurationDays();
        }

        // 一次性购买（积分包）：0天，表示不基于时间，永久有效
        if (BillingCycleEnum.ONE_TIME.getCode().equals(billingCycle)) {
            return 0;
        }

        // 兜底默认值，避免套餐配置缺失时出错
        log.warn("[calculatePaymentDuration][套餐({})未配置订阅时长，使用默认值30天]",
                plan != null ? plan.getId() : null);
        return 30;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubscriptionOrder(SubscriptionOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateSubscriptionOrderExists(updateReqVO.getId());

        // 获取原订单信息
        SubscriptionOrderDO originalOrder = subscriptionOrderMapper.selectById(updateReqVO.getId());

        // 更新订单
        SubscriptionOrderDO updateObj = BeanUtils.toBean(updateReqVO, SubscriptionOrderDO.class);
        subscriptionOrderMapper.updateById(updateObj);

        // 检查支付状态是否从非已支付变为已支付
        if (originalOrder.getPaymentStatus() != 20 && updateReqVO.getPaymentStatus() != null
                && updateReqVO.getPaymentStatus() == 20) {
            // 设置支付时间
            if (updateReqVO.getPaymentTime() == null) {
                updateObj.setPaymentTime(LocalDateTime.now());
                subscriptionOrderMapper.updateById(updateObj);
            }
            // 处理支付后业务逻辑
            processOrderPayment(updateObj);
        }
    }

    @Override
    public void deleteSubscriptionOrder(Long id) {
        // 校验存在
        validateSubscriptionOrderExists(id);
        // 删除
        subscriptionOrderMapper.deleteById(id);
    }

    private void validateSubscriptionOrderExists(Long id) {
        if (subscriptionOrderMapper.selectById(id) == null) {
            throw exception(SUBSCRIPTION_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public SubscriptionOrderDO getSubscriptionOrder(Long id) {
        return subscriptionOrderMapper.selectById(id);
    }

    @Override
    public PageResult<SubscriptionOrderDO> getSubscriptionOrderPage(SubscriptionOrderPageReqVO pageReqVO) {
        return subscriptionOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<SubscriptionOrderDO> getPaidOrdersByUserId(Long userId, PageParam pageReqVO) {
        SubscriptionOrderPageReqVO reqVO = new SubscriptionOrderPageReqVO();
        reqVO.setUserId(userId);
        reqVO.setPaymentStatus(20); // 已支付
        reqVO.setPageNo(pageReqVO.getPageNo());
        reqVO.setPageSize(pageReqVO.getPageSize());
        return subscriptionOrderMapper.selectPage(reqVO);
    }

    @Resource
    @Lazy
    private ReferralService referralService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFreeRewardOrder(Long userId, int days, Long planId, Integer subscriptionType) {
        log.info("[createFreeRewardOrder] 开始为用户({})赠送{}天时长, PlanId: {}, Type: {}", userId, days, planId,
                subscriptionType);

        if (planId == null || subscriptionType == null) {
            log.warn("[createFreeRewardOrder] 参数缺失，无法赠送。PlanId: {}, Type: {}", planId, subscriptionType);
            return;
        }

        // 1. 创建0元订单
        SubscriptionOrderDO order = new SubscriptionOrderDO();
        order.setOrderNo(cn.iocoder.yudao.framework.common.util.monitor.TracerUtils.getTraceId());
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo(java.util.UUID.randomUUID().toString().replace("-", ""));
        }
        order.setUserId(userId);
        order.setPlanId(planId);
        order.setSubscriptionType(subscriptionType);
        order.setBillingCycle(BillingCycleEnum.ONE_TIME.getCode()); // 一次性
        order.setOriginalPrice(java.math.BigDecimal.ZERO);
        order.setActualPrice(java.math.BigDecimal.ZERO);
        order.setPaymentStatus(20); // 直接已支付
        order.setPaymentTime(LocalDateTime.now());
        order.setPaymentMethod(30); // 其他

        subscriptionOrderMapper.insert(order);

        // 2. 触发订阅延期逻辑 (调用 upgradeSubscription)
        // 使用传入的 planId 和 subscriptionType，确保与原订单一致
        subscriptionService.upgradeSubscription(userId, subscriptionType, days, planId);

        log.info("[createFreeRewardOrder] 赠送订单创建完成, OrderId: {}", order.getId());
    }
}