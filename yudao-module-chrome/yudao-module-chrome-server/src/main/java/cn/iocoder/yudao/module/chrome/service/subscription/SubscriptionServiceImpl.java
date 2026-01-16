package cn.iocoder.yudao.module.chrome.service.subscription;

import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.module.chrome.service.plan.SubscriptionPlanService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.chrome.controller.admin.subscription.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.enums.BillingCycleEnum;
import cn.iocoder.yudao.module.chrome.enums.SubscriptionTypeEnum;
import cn.iocoder.yudao.module.chrome.service.credits.UserCreditsService;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import lombok.extern.slf4j.Slf4j;

import cn.iocoder.yudao.module.chrome.dal.mysql.subscription.SubscriptionMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * 插件订阅 Service 实现类
 *
 * @author Jax
 */
@Service
@Validated
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    @Resource
    private SubscriptionMapper subscriptionMapper;

    @Resource
    private UserCreditsService userCreditsService;

    @Resource
    private SubscriptionPlanService subscriptionPlanService;

    @Override
    public Long createSubscription(SubscriptionSaveReqVO createReqVO) {
        // 插入
        SubscriptionDO subscription = BeanUtils.toBean(createReqVO, SubscriptionDO.class);
        subscriptionMapper.insert(subscription);
        // 返回
        return subscription.getId();
    }

    @Override
    public void updateSubscription(SubscriptionSaveReqVO updateReqVO) {
        // 校验存在
        validateSubscriptionExists(updateReqVO.getId());
        // 更新
        SubscriptionDO updateObj = BeanUtils.toBean(updateReqVO, SubscriptionDO.class);
        subscriptionMapper.updateById(updateObj);
    }

    @Override
    public void deleteSubscription(Long id) {
        // 校验存在
        validateSubscriptionExists(id);
        // 删除
        subscriptionMapper.deleteById(id);
    }

    private void validateSubscriptionExists(Long id) {
        if (subscriptionMapper.selectById(id) == null) {
            throw exception(SUBSCRIPTION_NOT_EXISTS);
        }
    }

    @Override
    public SubscriptionDO getSubscription(Long id) {
        return subscriptionMapper.selectById(id);
    }

    @Override
    public PageResult<SubscriptionDO> getSubscriptionPage(SubscriptionPageReqVO pageReqVO) {
        return subscriptionMapper.selectPage(pageReqVO);
    }

    @Override
    public SubscriptionDO getActiveSubscriptionByUserId(Long userId) {
        return subscriptionMapper.selectActiveByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upgradeSubscription(Long userId, Integer newSubscriptionType, Integer paymentDuration, Long planId) {
        log.info("[upgradeSubscription][用户({})升级订阅, planId: {}, 时长({})天]", userId, planId, paymentDuration);

        // 1. 校验并获取套餐信息（planId 必填）
        if (planId == null) {
            log.error("[upgradeSubscription][planId不能为空]");
            throw exception(SUBSCRIPTION_PLAN_NOT_EXISTS);
        }

        SubscriptionPlanDO plan = subscriptionPlanService.getSubscriptionPlan(planId);
        if (plan == null) {
            log.error("[upgradeSubscription][套餐({})不存在]", planId);
            throw exception(SUBSCRIPTION_PLAN_NOT_EXISTS);
        }

        // 2. 从套餐配置获取所有必要信息
        Integer newBillingCycle = plan.getBillingCycle();
        newSubscriptionType = plan.getSubscriptionType();
        log.info("[upgradeSubscription][套餐({})信息: 计费周期={}, 订阅类型={}, 时长={}天]",
                planId, newBillingCycle, newSubscriptionType, paymentDuration);

        // 3. 获取当前有效订阅
        SubscriptionDO currentSubscription = getActiveSubscriptionByUserId(userId);

        // 4. 处理订阅逻辑
        if (currentSubscription != null) {
            return processSubscriptionUpgrade(userId, currentSubscription, newSubscriptionType, newBillingCycle,
                    paymentDuration, planId);
        } else {
            return createNewSubscription(userId, newSubscriptionType, newBillingCycle, paymentDuration, "首次订阅充值",
                    planId);
        }
    }

    /**
     * 处理订阅升级逻辑
     */
    private Long processSubscriptionUpgrade(Long userId, SubscriptionDO currentSubscription,
            Integer newSubscriptionType, Integer newBillingCycle, Integer paymentDuration, Long planId) {
        Integer currentType = currentSubscription.getSubscriptionType();
        Integer currentBillingCycle = currentSubscription.getBillingCycle();

        // 检查是否为相同套餐类型
        if (currentType.equals(newSubscriptionType)) {
            // 相同套餐类型，检查计费周期
            if (currentBillingCycle != null && currentBillingCycle.equals(newBillingCycle)) {
                // 相同套餐相同计费周期，检查是否为不同积分套餐
                return handleSameTypeSubscription(userId, currentSubscription, paymentDuration, planId);
            } else {
                // 相同套餐不同计费周期，检查优先级
                return handleBillingCycleChange(userId, currentSubscription, newSubscriptionType, newBillingCycle,
                        paymentDuration, planId);
            }
        } else {
            // 不同套餐类型，检查是否允许升级
            return handleSubscriptionTypeChange(userId, currentSubscription, newSubscriptionType, newBillingCycle,
                    paymentDuration, planId);
        }
    }

    /**
     * 处理相同套餐类型订阅（可能是不同积分套餐）
     */
    private Long handleSameTypeSubscription(Long userId, SubscriptionDO currentSubscription, Integer paymentDuration,
            Long planId) {
        // 如果planId相同，则为续费延长时间
        if (planId != null && planId.equals(currentSubscription.getPlanId())) {
            return extendSubscription(userId, currentSubscription, paymentDuration);
        }

        // 如果planId不同，说明是购买了不同积分的相同套餐类型
        // 这种情况下，我们采用替换策略：将原订阅设为无效，创建新订阅
        log.info("[handleSameTypeSubscription][用户({})购买了不同积分套餐，原planId: {}, 新planId: {}]",
                userId, currentSubscription.getPlanId(), planId);

        return replaceSubscription(userId, currentSubscription, currentSubscription.getSubscriptionType(),
                currentSubscription.getBillingCycle(), paymentDuration, "购买不同积分套餐", planId);
    }

    /**
     * 延长订阅时间（相同套餐相同计费周期）
     */
    private Long extendSubscription(Long userId, SubscriptionDO currentSubscription, Integer paymentDuration) {
        log.info("[extendSubscription][用户({})续费相同套餐，延长{}天]", userId, paymentDuration);

        // 计算新的结束时间：从当前结束时间开始延长，如果已过期则从当前时间开始
        LocalDateTime baseTime = currentSubscription.getEndTime().isAfter(LocalDateTime.now())
                ? currentSubscription.getEndTime()
                : LocalDateTime.now();
        LocalDateTime newEndTime = baseTime.plusDays(paymentDuration);

        currentSubscription.setEndTime(newEndTime);
        currentSubscription.setPaymentDuration(currentSubscription.getPaymentDuration() + paymentDuration);

        subscriptionMapper.updateById(currentSubscription);
        log.info("[extendSubscription][用户({})订阅({})续费成功，新结束时间({})]", userId, currentSubscription.getId(), newEndTime);

        // 根据订阅类型充值积分（续费时充值）
        rechargeCreditsForSubscription(userId, currentSubscription, "订阅续费充值");

        return currentSubscription.getId();
    }

    /**
     * 处理计费周期变更（相同套餐类型）
     */
    private Long handleBillingCycleChange(Long userId, SubscriptionDO currentSubscription,
            Integer subscriptionType, Integer newBillingCycle, Integer paymentDuration, Long planId) {
        Integer currentBillingCycle = currentSubscription.getBillingCycle();

        // 年付优先级高于月付
        if (BillingCycleEnum.YEARLY.getCode().equals(newBillingCycle) &&
                BillingCycleEnum.MONTHLY.getCode().equals(currentBillingCycle)) {
            // 月付升级到年付，允许
            log.info("[handleBillingCycleChange][用户({})从月付升级到年付]", userId);
            return replaceSubscription(userId, currentSubscription, subscriptionType, newBillingCycle, paymentDuration,
                    "月付升级年付充值", planId);
        } else if (BillingCycleEnum.MONTHLY.getCode().equals(newBillingCycle) &&
                BillingCycleEnum.YEARLY.getCode().equals(currentBillingCycle)) {
            // 年付降级到月付，拒绝
            log.warn("[handleBillingCycleChange][用户({})尝试从年付降级到月付，操作被拒绝]", userId);
            throw exception(SUBSCRIPTION_DOWNGRADE_NOT_ALLOWED);
        } else {
            // 其他情况，直接替换
            return replaceSubscription(userId, currentSubscription, subscriptionType, newBillingCycle, paymentDuration,
                    "订阅计费周期变更充值", planId);
        }
    }

    /**
     * 处理订阅类型变更
     */
    private Long handleSubscriptionTypeChange(Long userId, SubscriptionDO currentSubscription,
            Integer newSubscriptionType, Integer newBillingCycle, Integer paymentDuration, Long planId) {
        Integer currentType = currentSubscription.getSubscriptionType();

        // 检查订阅类型优先级：PREMIUM(30) > BASIC(20) > FREE(10)
        if (newSubscriptionType > currentType) {
            // 允许升级
            log.info("[handleSubscriptionTypeChange][用户({})从套餐类型({})升级到({})]", userId, currentType,
                    newSubscriptionType);
            return replaceSubscription(userId, currentSubscription, newSubscriptionType, newBillingCycle,
                    paymentDuration, "订阅套餐升级充值", planId);
        } else {
            // 拒绝降级
            log.warn("[handleSubscriptionTypeChange][用户({})尝试从套餐类型({})降级到({})，操作被拒绝]", userId, currentType,
                    newSubscriptionType);
            throw exception(SUBSCRIPTION_DOWNGRADE_NOT_ALLOWED);
        }
    }

    /**
     * 替换订阅（将当前订阅设为无效，创建新订阅）
     */
    private Long replaceSubscription(Long userId, SubscriptionDO currentSubscription,
            Integer newSubscriptionType, Integer newBillingCycle, Integer paymentDuration, String description,
            Long planId) {
        // 将当前订阅设为无效
        currentSubscription.setStatus(false);
        currentSubscription.setDeleted(true);
        subscriptionMapper.updateById(currentSubscription);
        log.info("[replaceSubscription][用户({})原订阅({})已设为无效]", userId, currentSubscription.getId());

        // 创建新订阅
        return createNewSubscription(userId, newSubscriptionType, newBillingCycle, paymentDuration, description,
                planId);
    }

    /**
     * 创建新订阅（带planId）
     */
    private Long createNewSubscription(Long userId, Integer subscriptionType, Integer billingCycle,
            Integer paymentDuration, String description, Long planId) {
        SubscriptionDO newSubscription = new SubscriptionDO();
        newSubscription.setUserId(userId);
        newSubscription.setSubscriptionType(subscriptionType);
        newSubscription.setBillingCycle(billingCycle);
        newSubscription.setPlanId(planId); // 设置planId
        newSubscription.setStatus(true);
        newSubscription.setStartTime(LocalDateTime.now());

        // 根据套餐类型和计费周期设置结束时间
        LocalDateTime endTime = calculateEndTime(subscriptionType, billingCycle, paymentDuration);
        newSubscription.setEndTime(endTime);
        newSubscription.setPaymentDuration(paymentDuration);
        newSubscription.setAutoRenew(false);

        subscriptionMapper.insert(newSubscription);
        log.info("[createNewSubscription][用户({})新订阅({})创建成功，planId: {}, 结束时间: {}]",
                userId, newSubscription.getId(), planId, endTime);

        // 根据订阅类型充值积分
        rechargeCreditsForSubscription(userId, newSubscription, description);

        return newSubscription.getId();
    }

    /**
     * 判断订阅是否为永久有效类型（不需要检查过期时间）
     * 
     * @param subscriptionType 订阅类型
     * @param billingCycle     计费周期
     * @return true=永久有效，不检查过期时间
     */
    private boolean isUnlimitedSubscription(Integer subscriptionType, Integer billingCycle) {
        // 免费版：永久有效
        if (SubscriptionTypeEnum.FREE.getCode().equals(subscriptionType)) {
            return true;
        }
        // 积分包套餐：一次性购买，永久有效
        if (SubscriptionTypeEnum.CREDITS_PACK.getCode().equals(subscriptionType)) {
            return true;
        }
        // 一次性购买计费周期：永久有效
        if (BillingCycleEnum.ONE_TIME.getCode().equals(billingCycle)) {
            return true;
        }
        return false;
    }

    /**
     * 计算订阅结束时间
     */
    private LocalDateTime calculateEndTime(Integer subscriptionType, Integer billingCycle, Integer paymentDuration) {
        LocalDateTime now = LocalDateTime.now();

        // 永久有效套餐：设置为100年后
        if (isUnlimitedSubscription(subscriptionType, billingCycle)) {
            return now.plusYears(100);
        }

        // 其他情况：按支付时长计算
        return now.plusDays(paymentDuration);
    }

    @Override
    public boolean validateSubscriptionStatus(Long userId) {
        SubscriptionDO subscription = getActiveSubscriptionByUserId(userId);
        if (subscription == null) {
            return false;
        }

        // 检查订阅状态
        if (!Boolean.TRUE.equals(subscription.getStatus())) {
            return false;
        }

        // 永久有效套餐不检查过期时间
        if (isUnlimitedSubscription(subscription.getSubscriptionType(), subscription.getBillingCycle())) {
            log.debug("[validateSubscriptionStatus][用户({})使用永久有效套餐，跳过有效期检查]", userId);
            return true;
        }

        // 检查是否过期（只对月付和年付套餐检查）
        if (subscription.getEndTime() != null && subscription.getEndTime().isBefore(LocalDateTime.now())) {
            // 自动将过期订阅设为无效
            subscription.setStatus(false);
            subscriptionMapper.updateById(subscription);
            log.info("[validateSubscriptionStatus][用户({})订阅({})已过期，自动设为无效]", userId, subscription.getId());
            return false;
        }

        return true;
    }

    @Override
    public List<SubscriptionDO> getExpiringSubscriptions(int days) {
        LocalDateTime expireTime = LocalDateTime.now().plusDays(days);
        return subscriptionMapper.selectExpiringSubscriptions(expireTime);
    }

    /**
     * 根据订阅类型为用户充值积分
     *
     * @param userId       用户ID
     * @param subscription 订阅信息
     * @param description  充值描述
     */
    private void rechargeCreditsForSubscription(Long userId, SubscriptionDO subscription, String description) {
        Integer credits = calculateCreditsForSubscription(subscription);
        if (credits > 0) {
            userCreditsService.rechargeCredits(userId, credits, subscription.getId().toString(), description);
            log.info("[rechargeCreditsForSubscription][用户({})订阅({})充值积分({})成功]",
                    userId, subscription.getId(), credits);
        }
    }

    /**
     * 计算订阅应充值的积分数（从套餐配置获取）
     *
     * @param subscription 订阅信息
     * @return 积分数
     */
    private Integer calculateCreditsForSubscription(SubscriptionDO subscription) {
        // 从套餐配置获取积分（planId 必填）
        if (subscription.getPlanId() == null) {
            log.warn("[calculateCreditsForSubscription][订阅({})无planId，无法获取积分]", subscription.getId());
            return 0;
        }

        SubscriptionPlanDO plan = subscriptionPlanService.getSubscriptionPlan(subscription.getPlanId());
        if (plan == null) {
            log.warn("[calculateCreditsForSubscription][套餐({})不存在]", subscription.getPlanId());
            return 0;
        }

        Integer credits = plan.getCredits();
        if (credits == null || credits <= 0) {
            log.debug("[calculateCreditsForSubscription][套餐({})积分为0]", plan.getPlanName());
            return 0;
        }

        log.debug("[calculateCreditsForSubscription][套餐({})积分: {}]", plan.getPlanName(), credits);
        return credits;
    }

    @Override
    public void invalidateUserSubscriptions(Long userId) {
        // 获取用户的所有订阅
        List<SubscriptionDO> subscriptions = subscriptionMapper.selectListByUserId(userId);
        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (SubscriptionDO subscription : subscriptions) {
                subscriptionMapper.deleteById(subscription);
            }
            log.info("[invalidateUserSubscriptions][作废用户订阅成功，用户ID: {}, 订阅数量: {}]", userId, subscriptions.size());
        }
    }

}