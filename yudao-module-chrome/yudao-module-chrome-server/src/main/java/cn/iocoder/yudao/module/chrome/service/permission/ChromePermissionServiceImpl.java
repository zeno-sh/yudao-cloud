package cn.iocoder.yudao.module.chrome.service.permission;

import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.enums.FeatureTypeEnum;
import cn.iocoder.yudao.module.chrome.enums.SubscriptionTypeEnum;
import cn.iocoder.yudao.module.chrome.service.credits.UserCreditsService;
import cn.iocoder.yudao.module.chrome.service.subscription.SubscriptionService;
import cn.iocoder.yudao.module.chrome.service.usage.UsageRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * Chrome权限校验 Service 实现类
 *
 * @author Jax
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChromePermissionServiceImpl implements ChromePermissionService {

    private final SubscriptionService subscriptionService;
    private final UsageRecordService usageRecordService;
    private final UserCreditsService userCreditsService;

    @Override
    public boolean validateFeaturePermission(Long userId, FeatureTypeEnum featureType) {
        return validateFeaturePermission(userId, featureType, 1);
    }

    @Override
    public boolean validateFeaturePermission(Long userId, FeatureTypeEnum featureType, Integer requiredCredits) {
        try {
            // 1. 校验订阅状态
            validateSubscriptionStatus(userId);
            
            // 2. 校验积分余额（不消费积分，只校验）
            if (!userCreditsService.hasEnoughCredits(userId, requiredCredits)) {
                log.warn("[validateFeaturePermission][用户({})积分不足，无法使用功能({})]", userId, featureType);
                return false;
            }

            log.info("[validateFeaturePermission][用户({})功能({})权限校验通过]", userId, featureType);
            return true;
            
        } catch (Exception e) {
            log.error("[validateFeaturePermission][用户({})功能({})权限校验失败]", userId, featureType, e);
            return false;
        }
    }

    @Override
    public SubscriptionDO validateSubscriptionStatus(Long userId) {
        // 获取用户当前有效订阅
        SubscriptionDO subscription = subscriptionService.getActiveSubscriptionByUserId(userId);
        
        if (subscription == null) {
            log.warn("[validateSubscriptionStatus][用户({})无有效订阅]", userId);
            throw exception(SUBSCRIPTION_NOT_FOUND);
        }
        
        // 校验订阅状态
        if (!Boolean.TRUE.equals(subscription.getStatus())) {
            log.warn("[validateSubscriptionStatus][用户({})订阅状态异常: {}]", userId, subscription.getStatus());
            throw exception(SUBSCRIPTION_EXPIRED);
        }
        
        // 校验订阅是否过期
        if (subscription.getEndTime() != null && subscription.getEndTime().isBefore(LocalDateTime.now())) {
            log.warn("[validateSubscriptionStatus][用户({})订阅已过期: {}]", userId, subscription.getEndTime());
            throw exception(SUBSCRIPTION_EXPIRED);
        }
        
        return subscription;
    }

    @Override
    public boolean validateUsageLimit(Long userId, FeatureTypeEnum featureType) {
        // 基于积分制，不再使用次数限制，而是检查积分余额
        return userCreditsService.hasEnoughCredits(userId, 1);
    }

    @Override
    public void recordUsage(Long userId, FeatureTypeEnum featureType) {
        try {
            usageRecordService.recordUsage(userId, featureType.getType());
            
            log.debug("[recordUsage][用户({})功能({})使用记录已保存]", userId, featureType);
        } catch (Exception e) {
            log.error("[recordUsage][用户({})功能({})使用记录保存失败]", userId, featureType, e);
            // 记录失败不影响主流程
        }
    }

    @Override
    public int getTodayUsage(Long userId, FeatureTypeEnum featureType) {
        return usageRecordService.getTodayUsageCount(userId, featureType.getType());
    }

    @Override
    public int getFeatureLimit(SubscriptionTypeEnum subscriptionType, FeatureTypeEnum featureType) {
        // 基于积分制，不再使用固定次数限制
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean hasFeaturePermission(SubscriptionTypeEnum subscriptionType, FeatureTypeEnum featureType) {
        // 所有订阅类型都可以使用功能，只要有积分
        return true;
    }


}