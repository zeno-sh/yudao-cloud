package cn.iocoder.yudao.module.chrome.service.subscription;

import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.plan.SubscriptionPlanMapper;
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
    private SubscriptionPlanMapper subscriptionPlanMapper;
    
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
    public Long upgradeSubscription(Long userId, Integer newSubscriptionType, Integer paymentDuration) {
        return upgradeSubscription(userId, newSubscriptionType, paymentDuration, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upgradeSubscription(Long userId, Integer newSubscriptionType, Integer paymentDuration, Long planId) {
        log.info("[upgradeSubscription][用户({})升级订阅到类型({}), 时长({})天, planId: {}]", userId, newSubscriptionType, paymentDuration, planId);
        
        // 1. 获取当前有效订阅
        SubscriptionDO currentSubscription = getActiveSubscriptionByUserId(userId);
        
        // 2. 通过planId获取套餐信息，直接使用套餐的计费周期
        Integer newBillingCycle;
        if (planId != null) {
            SubscriptionPlanDO plan = subscriptionPlanService.getSubscriptionPlan(planId);
            if (plan != null) {
                newBillingCycle = plan.getBillingCycle();
                // 确保订阅类型与套餐类型一致
                newSubscriptionType = plan.getSubscriptionType();
                log.info("[upgradeSubscription][从套餐({})获取计费周期: {}, 订阅类型: {}]", planId, newBillingCycle, newSubscriptionType);
            } else {
                log.warn("[upgradeSubscription][套餐({})不存在，使用默认计费周期]", planId);
                newBillingCycle = BillingCycleEnum.MONTHLY.getCode();
            }
        } else {
            // 兼容没有planId的情况，使用原有推断逻辑
            if (SubscriptionTypeEnum.CREDITS_PACK.getCode().equals(newSubscriptionType)) {
                newBillingCycle = BillingCycleEnum.ONE_TIME.getCode();
            } else if (paymentDuration == 0) {
                newBillingCycle = BillingCycleEnum.ONE_TIME.getCode();
            } else if (paymentDuration > 30) {
                newBillingCycle = BillingCycleEnum.YEARLY.getCode();
            } else {
                newBillingCycle = BillingCycleEnum.MONTHLY.getCode();
            }
            log.info("[upgradeSubscription][无planId，推断计费周期: {}, 订阅类型: {}, 时长: {}天]", newBillingCycle, newSubscriptionType, paymentDuration);
        }
        
        // 3. 如果有当前订阅，需要验证升级规则
        if (currentSubscription != null) {
            return processSubscriptionUpgrade(userId, currentSubscription, newSubscriptionType, newBillingCycle, paymentDuration, planId);
        } else {
            // 4. 没有当前订阅，直接创建新订阅
            return createNewSubscription(userId, newSubscriptionType, newBillingCycle, paymentDuration, "首次订阅充值", planId);
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
                return handleBillingCycleChange(userId, currentSubscription, newSubscriptionType, newBillingCycle, paymentDuration, planId);
            }
        } else {
            // 不同套餐类型，检查是否允许升级
            return handleSubscriptionTypeChange(userId, currentSubscription, newSubscriptionType, newBillingCycle, paymentDuration, planId);
        }
    }
    
    /**
     * 处理相同套餐类型订阅（可能是不同积分套餐）
     */
    private Long handleSameTypeSubscription(Long userId, SubscriptionDO currentSubscription, Integer paymentDuration, Long planId) {
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
            return replaceSubscription(userId, currentSubscription, subscriptionType, newBillingCycle, paymentDuration, "月付升级年付充值", planId);
        } else if (BillingCycleEnum.MONTHLY.getCode().equals(newBillingCycle) && 
                   BillingCycleEnum.YEARLY.getCode().equals(currentBillingCycle)) {
            // 年付降级到月付，拒绝
            log.warn("[handleBillingCycleChange][用户({})尝试从年付降级到月付，操作被拒绝]", userId);
            throw exception(SUBSCRIPTION_DOWNGRADE_NOT_ALLOWED);
        } else {
            // 其他情况，直接替换
            return replaceSubscription(userId, currentSubscription, subscriptionType, newBillingCycle, paymentDuration, "订阅计费周期变更充值", planId);
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
            log.info("[handleSubscriptionTypeChange][用户({})从套餐类型({})升级到({})]", userId, currentType, newSubscriptionType);
            return replaceSubscription(userId, currentSubscription, newSubscriptionType, newBillingCycle, paymentDuration, "订阅套餐升级充值", planId);
        } else {
            // 拒绝降级
            log.warn("[handleSubscriptionTypeChange][用户({})尝试从套餐类型({})降级到({})，操作被拒绝]", userId, currentType, newSubscriptionType);
            throw exception(SUBSCRIPTION_DOWNGRADE_NOT_ALLOWED);
        }
    }
    
    /**
     * 替换订阅（将当前订阅设为无效，创建新订阅）
     */
    private Long replaceSubscription(Long userId, SubscriptionDO currentSubscription, 
                                   Integer newSubscriptionType, Integer newBillingCycle, Integer paymentDuration, String description, Long planId) {
        // 将当前订阅设为无效
        currentSubscription.setStatus(false);
        currentSubscription.setDeleted(true);
        subscriptionMapper.updateById(currentSubscription);
        log.info("[replaceSubscription][用户({})原订阅({})已设为无效]", userId, currentSubscription.getId());
        
        // 创建新订阅
        return createNewSubscription(userId, newSubscriptionType, newBillingCycle, paymentDuration, description, planId);
    }
    
    /**
     * 创建新订阅（带planId）
     */
    private Long createNewSubscription(Long userId, Integer subscriptionType, Integer billingCycle, Integer paymentDuration, String description, Long planId) {
        SubscriptionDO newSubscription = new SubscriptionDO();
        newSubscription.setUserId(userId);
        newSubscription.setSubscriptionType(subscriptionType);
        newSubscription.setBillingCycle(billingCycle);
        newSubscription.setPlanId(planId);  // 设置planId
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
     * 计算订阅结束时间
     */
    private LocalDateTime calculateEndTime(Integer subscriptionType, Integer billingCycle, Integer paymentDuration) {
        LocalDateTime now = LocalDateTime.now();
        
        // 积分包套餐：永久有效（设置为100年后）
        if (SubscriptionTypeEnum.CREDITS_PACK.getCode().equals(subscriptionType)) {
            return now.plusYears(100);
        }
        
        // 一次性购买：永久有效（设置为100年后）
        if (BillingCycleEnum.ONE_TIME.getCode().equals(billingCycle)) {
            return now.plusYears(100);
        }
        
        // 其他情况：按支付时长计算
        return now.plusDays(paymentDuration);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long renewSubscription(Long userId, Integer paymentDuration) {
        log.info("[renewSubscription][用户({})续费订阅, 时长({})天]", userId, paymentDuration);
        
        // 1. 获取当前有效订阅
        SubscriptionDO currentSubscription = getActiveSubscriptionByUserId(userId);
        if (currentSubscription == null) {
            log.warn("[renewSubscription][用户({})无有效订阅，无法续费]", userId);
            throw exception(SUBSCRIPTION_NOT_EXISTS);
        }
        
        // 2. 延长订阅结束时间
        LocalDateTime newEndTime = currentSubscription.getEndTime().plusDays(paymentDuration);
        currentSubscription.setEndTime(newEndTime);
        currentSubscription.setPaymentDuration(currentSubscription.getPaymentDuration() + paymentDuration);
        
        subscriptionMapper.updateById(currentSubscription);
        log.info("[renewSubscription][用户({})订阅({})续费成功，新结束时间({})]", userId, currentSubscription.getId(), newEndTime);
        
        // 3. 根据订阅类型充值积分（续费时充值）
        rechargeCreditsForSubscription(userId, currentSubscription, "订阅续费充值");
        
        return currentSubscription.getId();
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
        
        // 积分包套餐不检查有效期（一次性购买，永久有效）
        if (SubscriptionTypeEnum.CREDITS_PACK.getCode().equals(subscription.getSubscriptionType())) {
            log.debug("[validateSubscriptionStatus][用户({})使用积分包套餐，跳过有效期检查]", userId);
            return true;
        }
        
        // 一次性购买的套餐不检查有效期
        if (BillingCycleEnum.ONE_TIME.getCode().equals(subscription.getBillingCycle())) {
            log.debug("[validateSubscriptionStatus][用户({})使用一次性购买套餐，跳过有效期检查]", userId);
            return true;
        }
        
        // 检查是否过期（只对月付和年付套餐检查）
        if (subscription.getEndTime() != null && subscription.getEndTime().isBefore(LocalDateTime.now())) {
            // 自动将过期订阅设为无效
            subscription.setStatus(false); // 0表示无效
            subscriptionMapper.updateById(subscription);
            log.info("[validateSubscriptionStatus][用户({})订阅({})已过期，自动设为无效]", userId, subscription.getId());
            return false;
        }
        
        return true;
    }

    @Override
    public Map<String, Object> getSubscriptionStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. 获取当前订阅信息
        SubscriptionDO currentSubscription = getActiveSubscriptionByUserId(userId);
        if (currentSubscription != null) {
            stats.put("subscriptionType", currentSubscription.getSubscriptionType());
            stats.put("subscriptionTypeName", SubscriptionTypeEnum.valueOf(currentSubscription.getSubscriptionType()).getName());
            stats.put("status", currentSubscription.getStatus());
            stats.put("startTime", currentSubscription.getStartTime());
            stats.put("endTime", currentSubscription.getEndTime());
            stats.put("paymentDuration", currentSubscription.getPaymentDuration());
            stats.put("autoRenew", currentSubscription.getAutoRenew());
            
            // 计算剩余天数
            if (currentSubscription.getEndTime() != null) {
                // 积分包套餐和永久套餐显示为无限制
                if (SubscriptionTypeEnum.CREDITS_PACK.getCode().equals(currentSubscription.getSubscriptionType()) ||
                    BillingCycleEnum.ONE_TIME.getCode().equals(currentSubscription.getBillingCycle())) {
                    stats.put("remainingDays", -1); // -1表示无限制
                    stats.put("isUnlimited", true);
                } else {
                    long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), currentSubscription.getEndTime());
                    stats.put("remainingDays", Math.max(0, remainingDays));
                    stats.put("isUnlimited", false);
                }
            }
        } else {
            stats.put("subscriptionType", SubscriptionTypeEnum.FREE.getCode());
            stats.put("subscriptionTypeName", SubscriptionTypeEnum.FREE.getName());
            stats.put("status", false); // 0表示无效
            stats.put("remainingDays", 0);
        }
        
        // 2. 获取使用统计（今日使用次数）
        // 这里可以根据需要添加使用统计逻辑
        stats.put("todayUsageCount", 0); // 占位符，可以后续实现
        
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processAutoRenewal(Long userId) {
        log.info("[processAutoRenewal][处理用户({})自动续费]", userId);
        
        SubscriptionDO subscription = getActiveSubscriptionByUserId(userId);
        if (subscription == null || !subscription.getAutoRenew()) {
            log.info("[processAutoRenewal][用户({})无需自动续费]", userId);
            return false;
        }
        
        // 检查是否即将过期（提前3天）
        LocalDateTime threeDaysLater = LocalDateTime.now().plusDays(3);
        if (subscription.getEndTime().isAfter(threeDaysLater)) {
            log.info("[processAutoRenewal][用户({})订阅未到续费时间]", userId);
            return false;
        }
        
        try {
            // 这里应该调用支付接口进行自动扣费
            // 暂时模拟续费成功，续费30天
            renewSubscription(userId, 30);
            log.info("[processAutoRenewal][用户({})自动续费成功]", userId);
            return true;
        } catch (Exception e) {
            log.error("[processAutoRenewal][用户({})自动续费失败]", userId, e);
            return false;
        }
    }

    @Override
    public List<SubscriptionDO> getExpiringSubscriptions(int days) {
        LocalDateTime expireTime = LocalDateTime.now().plusDays(days);
        return subscriptionMapper.selectExpiringSubscriptions(expireTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long downgradeExpiredSubscription(Long userId, Integer targetSubscriptionType, Integer duration) {
        log.info("[downgradeExpiredSubscription][用户({})订阅到期降级到类型({}), 时长({})天]", userId, targetSubscriptionType, duration);
        
        // 1. 获取目标订阅类型的默认套餐信息
        SubscriptionPlanDO targetPlan = getDefaultPlanForSubscriptionType(targetSubscriptionType);
        Integer targetBillingCycle = targetPlan != null ? targetPlan.getBillingCycle() : BillingCycleEnum.YEARLY.getCode();
        Long targetPlanId = targetPlan != null ? targetPlan.getId() : null;
        
        log.info("[downgradeExpiredSubscription][目标套餐信息: planId={}, billingCycle={}]", targetPlanId, targetBillingCycle);
        
        // 2. 获取当前有效订阅
        SubscriptionDO currentSubscription = getActiveSubscriptionByUserId(userId);
        if (currentSubscription == null) {
            log.warn("[downgradeExpiredSubscription][用户({})无有效订阅，直接创建降级订阅]", userId);
            // 直接创建降级订阅
            return createNewSubscription(userId, targetSubscriptionType, 
                targetBillingCycle, duration, "订阅到期自动降级", targetPlanId);
        }
        
        // 3. 将当前订阅设为无效（到期处理）
        currentSubscription.setStatus(false);
        currentSubscription.setDeleted(true);
        subscriptionMapper.updateById(currentSubscription);
        log.info("[downgradeExpiredSubscription][用户({})原订阅({})已到期设为无效]", userId, currentSubscription.getId());
        
        // 4. 创建新的降级订阅（绕过权限检查）
        Long newSubscriptionId = createNewSubscription(userId, targetSubscriptionType, 
            targetBillingCycle, duration, "订阅到期自动降级", targetPlanId);
        
        log.info("[downgradeExpiredSubscription][用户({})订阅到期降级完成，新订阅ID: {}]", userId, newSubscriptionId);
        return newSubscriptionId;
    }
    
    /**
     * 获取指定订阅类型的默认套餐
     */
    private SubscriptionPlanDO getDefaultPlanForSubscriptionType(Integer subscriptionType) {
        // 查询指定订阅类型的第一个启用套餐作为默认套餐
        return subscriptionPlanService.getEnabledSubscriptionPlans().stream()
            .filter(plan -> subscriptionType.equals(plan.getSubscriptionType()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据订阅类型为用户充值积分
     *
     * @param userId 用户ID
     * @param subscription 订阅信息
     * @param description 充值描述
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
     * 计算订阅应充值的积分数
     *
     * @param subscription 订阅信息
     * @return 积分数
     */
    private Integer calculateCreditsForSubscription(SubscriptionDO subscription) {
        log.debug("[calculateCreditsForSubscription][计算订阅({})积分，planId: {}]", subscription.getId(), subscription.getPlanId());
        
        try {
            // 1. 如果订阅表中直接有积分，优先使用
            if (subscription.getCredits() != null && subscription.getCredits() > 0) {
                log.debug("[calculateCreditsForSubscription][使用订阅表积分: {}]", subscription.getCredits());
                return subscription.getCredits();
            }
            
            // 2. 如果有planId，查询套餐表获取积分
            if (subscription.getPlanId() != null) {
                SubscriptionPlanDO plan = subscriptionPlanMapper.selectById(subscription.getPlanId());
                if (plan != null && plan.getCredits() > 0) {
                    log.debug("[calculateCreditsForSubscription][使用套餐表积分，套餐: {}, 积分: {}]", 
                        plan.getPlanName(), plan.getCredits());
                    return plan.getCredits();
                }
            }
            
            // 3. 兜底：根据订阅类型和计费周期查询套餐表
            SubscriptionPlanDO plan = subscriptionPlanMapper.selectOne(
                    SubscriptionPlanDO::getSubscriptionType, subscription.getSubscriptionType(),
                    SubscriptionPlanDO::getBillingCycle, subscription.getBillingCycle()
            );
            
            if (plan != null && plan.getCredits() > 0) {
                log.debug("[calculateCreditsForSubscription][使用匹配套餐积分，套餐: {}, 积分: {}]", 
                    plan.getPlanName(), plan.getCredits());
                return plan.getCredits();
            }
            
            // 4. 未找到套餐配置，返回0
            log.warn("[calculateCreditsForSubscription][未找到套餐配置，返回0积分，订阅类型: {}, 计费周期: {}]", 
                subscription.getSubscriptionType(), subscription.getBillingCycle());
            return 0;
            
        } catch (Exception e) {
            log.error("[calculateCreditsForSubscription][计算积分失败，订阅: {}]", subscription.getId(), e);
            return 0;
        }
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