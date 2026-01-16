package cn.iocoder.yudao.module.chrome.service.credits;

import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.plan.SubscriptionPlanMapper;
import cn.iocoder.yudao.module.chrome.dal.mysql.subscription.SubscriptionMapper;
import cn.iocoder.yudao.module.chrome.enums.SubscriptionTypeEnum;
import cn.iocoder.yudao.module.chrome.enums.BillingCycleEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * 积分包辅助服务
 * 提供积分包相关的查询和验证功能，不处理购买逻辑
 *
 * @author Jax
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditsPackService {

    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final SubscriptionMapper subscriptionMapper;

    /**
     * 获取所有可用的积分包套餐
     */
    public List<SubscriptionPlanDO> getAvailableCreditsPackPlans() {
        return subscriptionPlanMapper.selectList(
                new LambdaQueryWrapper<SubscriptionPlanDO>()
                        .eq(SubscriptionPlanDO::getSubscriptionType, SubscriptionTypeEnum.CREDITS_PACK.getCode())
                        .eq(SubscriptionPlanDO::getBillingCycle, BillingCycleEnum.ONE_TIME.getCode())
                        .eq(SubscriptionPlanDO::getStatus, true)
                        .orderByAsc(SubscriptionPlanDO::getSortOrder));
    }

    /**
     * 验证是否为积分包套餐
     */
    public boolean isCreditsPackPlan(Long planId) {
        if (planId == null) {
            return false;
        }

        SubscriptionPlanDO plan = subscriptionPlanMapper.selectById(planId);
        return plan != null &&
                SubscriptionTypeEnum.CREDITS_PACK.getCode().equals(plan.getSubscriptionType()) &&
                BillingCycleEnum.ONE_TIME.getCode().equals(plan.getBillingCycle());
    }

    /**
     * 验证积分包套餐
     */
    public SubscriptionPlanDO validateCreditsPackPlan(Long planId) {
        SubscriptionPlanDO plan = subscriptionPlanMapper.selectById(planId);
        if (plan == null) {
            throw new ServiceException(SUBSCRIPTION_PLAN_NOT_EXISTS);
        }

        if (!SubscriptionTypeEnum.CREDITS_PACK.getCode().equals(plan.getSubscriptionType())) {
            throw new ServiceException(SUBSCRIPTION_PLAN_TYPE_ERROR.getCode(),
                    "该套餐不是积分包类型");
        }

        if (!Boolean.TRUE.equals(plan.getStatus())) {
            throw new ServiceException(SUBSCRIPTION_PLAN_DISABLED);
        }

        return plan;
    }

    /**
     * 检查用户是否有积分包订阅
     */
    public boolean hasCreditsPackSubscription(Long userId) {
        SubscriptionDO subscription = subscriptionMapper.selectOne(
                new LambdaQueryWrapper<SubscriptionDO>()
                        .eq(SubscriptionDO::getUserId, userId)
                        .eq(SubscriptionDO::getSubscriptionType, SubscriptionTypeEnum.CREDITS_PACK.getCode())
                        .eq(SubscriptionDO::getStatus, true));
        return subscription != null;
    }

    /**
     * 获取用户的积分包订阅信息
     */
    public SubscriptionDO getUserCreditsPackSubscription(Long userId) {
        return subscriptionMapper.selectOne(
                new LambdaQueryWrapper<SubscriptionDO>()
                        .eq(SubscriptionDO::getUserId, userId)
                        .eq(SubscriptionDO::getSubscriptionType, SubscriptionTypeEnum.CREDITS_PACK.getCode())
                        .eq(SubscriptionDO::getStatus, true));
    }
}
