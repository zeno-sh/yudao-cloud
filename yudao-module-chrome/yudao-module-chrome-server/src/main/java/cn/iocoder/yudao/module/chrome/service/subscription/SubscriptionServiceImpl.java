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

    @Override
    public java.math.BigDecimal calculateRemainingValue(Long userId) {
        // 1. 获取当前有效订阅
        SubscriptionDO subscription = getActiveSubscriptionByUserId(userId);
        if (subscription == null || subscription.getPlanId() == null) {
            log.debug("[calculateRemainingValue][用户({})无有效订阅，剩余价值为0]", userId);
            return java.math.BigDecimal.ZERO;
        }

        // 2. 获取原套餐信息
        SubscriptionPlanDO plan = subscriptionPlanService.getSubscriptionPlan(subscription.getPlanId());
        if (plan == null) {
            log.warn("[calculateRemainingValue][套餐({})不存在]", subscription.getPlanId());
            return java.math.BigDecimal.ZERO;
        }

        // 3. 免费版无剩余价值
        if (SubscriptionTypeEnum.FREE.getCode().equals(subscription.getSubscriptionType())) {
            log.debug("[calculateRemainingValue][用户({})为免费版，剩余价值为0]", userId);
            return java.math.BigDecimal.ZERO;
        }

        // 4. 计算剩余天数
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = subscription.getEndTime();
        if (endTime == null || endTime.isBefore(now)) {
            log.debug("[calculateRemainingValue][用户({})订阅已过期，剩余价值为0]", userId);
            return java.math.BigDecimal.ZERO;
        }
        long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(now, endTime);

        // 5. 计算剩余价值 = 原价 * (剩余天数 / 总天数)
        Integer totalDays = plan.getDurationDays();
        if (totalDays == null || totalDays <= 0) {
            log.warn("[calculateRemainingValue][套餐({})未配置有效期天数]", plan.getId());
            return java.math.BigDecimal.ZERO;
        }

        java.math.BigDecimal price = plan.getDiscountedPrice() != null ? plan.getDiscountedPrice() : plan.getPrice();
        if (price == null) {
            return java.math.BigDecimal.ZERO;
        }

        java.math.BigDecimal remainingValue = price
                .multiply(java.math.BigDecimal.valueOf(remainingDays))
                .divide(java.math.BigDecimal.valueOf(totalDays), 2, java.math.RoundingMode.HALF_UP);

        log.info("[calculateRemainingValue][用户({})套餐({})剩余{}天/共{}天，剩余价值: {}元]",
                userId, plan.getPlanName(), remainingDays, totalDays, remainingValue);

        return remainingValue;
    }

    @Override
    public cn.iocoder.yudao.module.chrome.controller.plugin.subscription.vo.UpgradePriceVO calculateUpgradePrice(
            Long userId, Long targetPlanId) {
        // 1. 获取目标套餐
        SubscriptionPlanDO targetPlan = subscriptionPlanService.getSubscriptionPlan(targetPlanId);
        if (targetPlan == null) {
            throw exception(SUBSCRIPTION_PLAN_NOT_EXISTS);
        }

        // 2. 获取当前订阅信息
        SubscriptionDO currentSubscription = getActiveSubscriptionByUserId(userId);

        // 3. 计算目标套餐价格（元）
        java.math.BigDecimal targetPrice = targetPlan.getDiscountedPrice() != null
                ? targetPlan.getDiscountedPrice()
                : targetPlan.getPrice();

        // 4. 判断购买类型
        String message;

        if (currentSubscription == null) {
            // 新用户首购
            message = "首次订阅" + targetPlan.getPlanName();
            return buildUpgradePriceVO(targetPlanId, targetPlan.getPlanName(), targetPrice,
                    java.math.BigDecimal.ZERO, targetPrice, true, true, message);
        }

        // 5. 判断是续费还是升级
        if (targetPlanId.equals(currentSubscription.getPlanId())) {
            // 相同套餐：续费
            message = "续费" + targetPlan.getPlanName() + "，时间将叠加";
            return buildUpgradePriceVO(targetPlanId, targetPlan.getPlanName(), targetPrice,
                    java.math.BigDecimal.ZERO, targetPrice, true, true, message);
        }

        // 6. 获取当前套餐信息用于比较
        SubscriptionPlanDO currentPlan = currentSubscription.getPlanId() != null
                ? subscriptionPlanService.getSubscriptionPlan(currentSubscription.getPlanId())
                : null;
        String currentPlanName = currentPlan != null ? currentPlan.getPlanName() : "当前套餐";

        // 7. 判断是升级还是降级
        Integer currentType = currentSubscription.getSubscriptionType();
        Integer targetType = targetPlan.getSubscriptionType();
        Integer currentCycle = currentSubscription.getBillingCycle();
        Integer targetCycle = targetPlan.getBillingCycle();

        // 类型降级：禁止
        if (targetType < currentType) {
            message = "不支持从高级套餐降级到低级套餐，请等当前套餐到期后再购买";
            return buildUpgradePriceVO(targetPlanId, targetPlan.getPlanName(), targetPrice,
                    java.math.BigDecimal.ZERO, targetPrice, false, false, message);
        }

        // 同类型周期降级：禁止（年付降到月付）
        if (targetType.equals(currentType) && targetCycle != null && currentCycle != null
                && targetCycle < currentCycle) {
            message = "不支持从年付降级到月付，请等当前套餐到期后再购买";
            return buildUpgradePriceVO(targetPlanId, targetPlan.getPlanName(), targetPrice,
                    java.math.BigDecimal.ZERO, targetPrice, false, false, message);
        }

        // 8. 升级场景：计算差价
        java.math.BigDecimal remainingValue = calculateRemainingValue(userId);
        java.math.BigDecimal upgradePrice = targetPrice.subtract(remainingValue);

        // 差价不能为负
        if (upgradePrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
            upgradePrice = java.math.BigDecimal.ZERO;
        }

        if (upgradePrice.compareTo(java.math.BigDecimal.ZERO) == 0) {
            message = "从" + currentPlanName + "升级到" + targetPlan.getPlanName() + "，无需额外付费";
        } else {
            message = "从" + currentPlanName + "升级到" + targetPlan.getPlanName()
                    + "，原套餐剩余价值¥" + remainingValue.setScale(2, java.math.RoundingMode.HALF_UP)
                    + "，只需支付差价";
        }

        log.info("[calculateUpgradePrice][用户({})升级: {} -> {}, 目标价格:{}, 剩余价值:{}, 差价:{}]",
                userId, currentPlanName, targetPlan.getPlanName(), targetPrice, remainingValue, upgradePrice);

        return buildUpgradePriceVO(targetPlanId, targetPlan.getPlanName(), targetPrice,
                remainingValue, upgradePrice, false, true, message);
    }

    /**
     * 构建升级价格VO
     */
    private cn.iocoder.yudao.module.chrome.controller.plugin.subscription.vo.UpgradePriceVO buildUpgradePriceVO(
            Long targetPlanId, String targetPlanName, java.math.BigDecimal originalPrice,
            java.math.BigDecimal remainingValue, java.math.BigDecimal upgradePrice,
            Boolean isRenewal, Boolean isUpgrade, String message) {
        return cn.iocoder.yudao.module.chrome.controller.plugin.subscription.vo.UpgradePriceVO.builder()
                .targetPlanId(targetPlanId)
                .targetPlanName(targetPlanName)
                .originalPrice(originalPrice)
                .remainingValue(remainingValue)
                .upgradePrice(upgradePrice)
                .isRenewal(isRenewal)
                .isUpgrade(isUpgrade)
                .message(message)
                .build();
    }

}