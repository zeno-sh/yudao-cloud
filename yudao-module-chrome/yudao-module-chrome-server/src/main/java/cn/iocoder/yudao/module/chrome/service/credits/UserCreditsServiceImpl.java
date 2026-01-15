package cn.iocoder.yudao.module.chrome.service.credits;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.chrome.controller.admin.credits.vo.*;
import cn.iocoder.yudao.module.chrome.controller.admin.transaction.vo.CreditsTransactionSaveReqVO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.credits.UserCreditsDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.service.transaction.CreditsTransactionService;
import cn.iocoder.yudao.module.chrome.enums.TransactionTypeEnum;
import cn.iocoder.yudao.module.chrome.enums.FeatureTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import cn.iocoder.yudao.module.chrome.dal.mysql.credits.UserCreditsMapper;
import cn.iocoder.yudao.module.chrome.dal.mysql.subscription.SubscriptionMapper;
import cn.iocoder.yudao.module.chrome.dal.mysql.plan.SubscriptionPlanMapper;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * 用户积分账户 Service 实现类
 *
 * @author Jax
 */
@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserCreditsServiceImpl implements UserCreditsService {

    private final UserCreditsMapper userCreditsMapper;
    private final CreditsTransactionService creditsTransactionService;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public Long createUserCredits(UserCreditsSaveReqVO createReqVO) {
        // 插入
        UserCreditsDO userCredits = BeanUtils.toBean(createReqVO, UserCreditsDO.class);
        userCreditsMapper.insert(userCredits);
        // 返回
        return userCredits.getId();
    }

    @Override
    public void updateUserCredits(UserCreditsSaveReqVO updateReqVO) {
        // 校验存在
        validateUserCreditsExists(updateReqVO.getId());
        // 更新
        UserCreditsDO updateObj = BeanUtils.toBean(updateReqVO, UserCreditsDO.class);
        userCreditsMapper.updateById(updateObj);
    }

    @Override
    public void deleteUserCredits(Long id) {
        // 校验存在
        validateUserCreditsExists(id);
        // 删除
        userCreditsMapper.deleteById(id);
    }

    private void validateUserCreditsExists(Long id) {
        if (userCreditsMapper.selectById(id) == null) {
            throw exception(USER_CREDITS_NOT_EXISTS);
        }
    }

    @Override
    public UserCreditsDO getUserCredits(Long id) {
        return userCreditsMapper.selectById(id);
    }

    @Override
    public PageResult<UserCreditsDO> getUserCreditsPage(UserCreditsPageReqVO pageReqVO) {
        return userCreditsMapper.selectPage(pageReqVO);
    }

    // ========== 以下是业务方法实现 ==========

    @Override
    public UserCreditsDO getUserCreditsByUserId(Long userId) {
        return userCreditsMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCreditsDO initUserCredits(Long userId) {
        log.info("[initUserCredits][初始化用户({})积分账户]", userId);
        
        // 检查是否已存在
        UserCreditsDO existingCredits = getUserCreditsByUserId(userId);
        if (existingCredits != null) {
            log.info("[initUserCredits][用户({})积分账户已存在，返回现有账户]", userId);
            return existingCredits;
        }
        
        // 创建新的积分账户
        UserCreditsDO userCredits = new UserCreditsDO();
        userCredits.setUserId(userId);
        userCredits.setTotalCredits(10); // 免费版初始10积分
        userCredits.setUsedCredits(0);
        userCredits.setRemainingCredits(10);

        userCreditsMapper.insert(userCredits);
        log.info("[initUserCredits][用户({})积分账户初始化成功，初始积分: 10]", userId);
        
        return userCredits;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rechargeCredits(Long userId, Integer credits, String businessId, String description) {
        log.info("[rechargeCredits][用户({})充值积分: {}, 业务ID: {}, 描述: {}]", userId, credits, businessId, description);
        
        try {
            // 1. 获取或初始化用户积分账户
            UserCreditsDO userCredits = getUserCreditsByUserId(userId);
            if (userCredits == null) {
                userCredits = initUserCredits(userId);
            }
            
            // 2. 记录充值前的剩余积分（重要：必须在更新前记录）
            Integer beforeRemainingCredits = userCredits.getRemainingCredits();
            
            // 3. 更新积分
            userCredits.setTotalCredits(userCredits.getTotalCredits() + credits);
            userCredits.setRemainingCredits(userCredits.getRemainingCredits() + credits);
            userCredits.setUpdateTime(LocalDateTime.now());
            
            userCreditsMapper.updateById(userCredits);
            
            // 4. 记录交易（使用充值前后的剩余积分）
            CreditsTransactionSaveReqVO transactionReqVO = new CreditsTransactionSaveReqVO();
            transactionReqVO.setUserId(userId);
            transactionReqVO.setCreditsAmount(credits);
            transactionReqVO.setTransactionType(TransactionTypeEnum.RECHARGE.getCode());
            transactionReqVO.setBeforeCredits(beforeRemainingCredits); // 修复：使用充值前的剩余积分
            transactionReqVO.setAfterCredits(userCredits.getRemainingCredits()); // 修复：使用充值后的剩余积分
            transactionReqVO.setBusinessType(1); // RECHARGE类型
            transactionReqVO.setBusinessId(businessId);
            transactionReqVO.setDescription(description != null ? description : "积分充值");
            
            creditsTransactionService.createCreditsTransaction(transactionReqVO);
            
            log.info("[rechargeCredits][用户({})充值积分成功，当前余额: {}]", userId, userCredits.getRemainingCredits());
            return true;
            
        } catch (Exception e) {
            log.error("[rechargeCredits][用户({})充值积分失败]", userId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumeCredits(Long userId, Integer credits, Integer featureType, String businessId) {
        log.info("[consumeCredits][用户({})消费积分: {}, 功能类型: {}, 业务ID: {}]", userId, credits, featureType, businessId);
        
        try {
            // 1. 获取用户积分账户
            UserCreditsDO userCredits = getUserCreditsByUserId(userId);
            if (userCredits == null) {
                log.warn("[consumeCredits][用户({})积分账户不存在]", userId);
                return false;
            }
            
            // 2. 检查余额是否足够
            if (userCredits.getRemainingCredits() < credits) {
                log.warn("[consumeCredits][用户({})积分余额不足，当前余额: {}, 需要: {}]", 
                    userId, userCredits.getRemainingCredits(), credits);
                return false;
            }
            
            // 3. 扣减积分
            userCredits.setUsedCredits(userCredits.getUsedCredits() + credits);
            userCredits.setRemainingCredits(userCredits.getRemainingCredits() - credits);
            userCredits.setUpdateTime(LocalDateTime.now());
            
            userCreditsMapper.updateById(userCredits);
            
            // 4. 记录交易
            CreditsTransactionSaveReqVO transactionReqVO = new CreditsTransactionSaveReqVO();
            transactionReqVO.setUserId(userId);
            transactionReqVO.setCreditsAmount(-credits); // 消费为负数
            transactionReqVO.setTransactionType(TransactionTypeEnum.CONSUME.getCode());
            transactionReqVO.setBeforeCredits(userCredits.getRemainingCredits() + credits);
            transactionReqVO.setAfterCredits(userCredits.getRemainingCredits());
            transactionReqVO.setBusinessType(featureType); // CONSUME类型
            transactionReqVO.setBusinessId(businessId);
            // 根据功能类型获取描述名称
            String featureName = getFeatureTypeName(featureType);
            transactionReqVO.setDescription(String.format("使用%s功能消费积分", featureName));
            
            creditsTransactionService.createCreditsTransaction(transactionReqVO);
            
            log.info("[consumeCredits][用户({})消费积分成功，剩余积分: {}]", userId, userCredits.getRemainingCredits());
            return true;
            
        } catch (Exception e) {
            log.error("[consumeCredits][用户({})消费积分失败]", userId, e);
            return false;
        }
    }

    @Override
    public boolean hasEnoughCredits(Long userId, Integer requiredCredits) {
        UserCreditsDO userCredits = getUserCreditsByUserId(userId);
        if (userCredits == null) {
            return false;
        }
        return userCredits.getRemainingCredits() >= requiredCredits;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetCredits(Long userId, Integer newCredits) {
        log.info("[resetCredits][重置用户({})积分为: {}]", userId, newCredits);
        
        try {
            UserCreditsDO userCredits = getUserCreditsByUserId(userId);
            if (userCredits == null) {
                userCredits = initUserCredits(userId);
            }
            
            // 重置积分
            userCredits.setTotalCredits(newCredits);
            userCredits.setUsedCredits(0);
            userCredits.setRemainingCredits(newCredits);
            userCredits.setUpdateTime(LocalDateTime.now());
            
            userCreditsMapper.updateById(userCredits);
            
            log.info("[resetCredits][用户({})积分重置成功]", userId);
            return true;
            
        } catch (Exception e) {
            log.error("[resetCredits][用户({})积分重置失败]", userId, e);
            return false;
        }
    }

    @Override
    public Integer getRemainingCredits(Long userId) {
        UserCreditsDO userCredits = getUserCreditsByUserId(userId);
        return userCredits != null ? userCredits.getRemainingCredits() : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchResetMonthlyUserCredits() {
        log.info("[batchResetMonthlyUserCredits][批量重置月付用户积分 - 基于购买时间]");
        
        try {
            // 1. 获取今天需要重置积分的月付用户ID列表
            List<Long> userIds = userCreditsMapper.selectMonthlyUserIdsForReset();
            if (userIds.isEmpty()) {
                log.info("[batchResetMonthlyUserCredits][今天没有需要重置的月付用户]");
                return 0;
            }
            
            // 2. 获取这些用户的积分账户
            List<UserCreditsDO> monthlyUserCredits = userCreditsMapper.selectByUserIds(userIds);
            
            int resetCount = 0;
            for (UserCreditsDO userCredits : monthlyUserCredits) {
                // 根据用户的套餐计算应重置的积分数
                Integer newCredits = calculateMonthlyCredits(userCredits.getUserId());
                if (newCredits > 0) {
                    userCredits.setTotalCredits(newCredits);
                    userCredits.setUsedCredits(0);
                    userCredits.setRemainingCredits(newCredits);
                    userCredits.setUpdateTime(LocalDateTime.now());
                    
                    userCreditsMapper.updateById(userCredits);
                    resetCount++;
                    
                    log.info("[batchResetMonthlyUserCredits][用户({})积分重置成功，新积分: {}]", 
                        userCredits.getUserId(), newCredits);
                }
            }
            
            log.info("[batchResetMonthlyUserCredits][基于购买时间的月付用户积分重置完成，重置用户数: {}]", resetCount);
            return resetCount;
            
        } catch (Exception e) {
            log.error("[batchResetMonthlyUserCredits][批量重置月付用户积分失败]", e);
            return 0;
        }
    }

    /**
     * 计算月付用户应重置的积分数
     * 根据用户的实际套餐计算积分，而不是写死的值
     *
     * @param userId 用户ID
     * @return 积分数
     */
    private Integer calculateMonthlyCredits(Long userId) {
        log.debug("[calculateMonthlyCredits][计算用户({})月付积分]", userId);
        
        try {
            // 1. 获取用户的有效订阅信息
            SubscriptionDO subscription = subscriptionMapper.selectActiveByUserId(userId);
            if (subscription == null) {
                log.warn("[calculateMonthlyCredits][用户({})没有有效订阅]", userId);
                return 0;
            }
            
            // 2. 如果订阅表中直接有积分，优先使用
            if (subscription.getCredits() != null && subscription.getCredits() > 0) {
                log.debug("[calculateMonthlyCredits][用户({})使用订阅表积分: {}]", userId, subscription.getCredits());
                return subscription.getCredits();
            }
            
            // 3. 如果有plan_id，查询套餐表获取积分
            if (subscription.getPlanId() != null) {
                SubscriptionPlanDO plan = subscriptionPlanMapper.selectById(subscription.getPlanId());
                if (plan != null && plan.getCredits() > 0) {
                    log.debug("[calculateMonthlyCredits][用户({})使用套餐表积分，套餐: {}, 积分: {}]", 
                        userId, plan.getPlanName(), plan.getCredits());
                    return plan.getCredits();
                }
            }
            
            // 4. 兜底：根据订阅类型和计费周期查询套餐表
            SubscriptionPlanDO plan = subscriptionPlanMapper.selectOne(
                    SubscriptionPlanDO::getSubscriptionType, subscription.getSubscriptionType(),
                    SubscriptionPlanDO::getBillingCycle, subscription.getBillingCycle()
            );

            if (plan != null && plan.getCredits() > 0) {
                log.debug("[calculateMonthlyCredits][用户({})使用匹配套餐积分，套餐: {}, 积分: {}]", 
                    userId, plan.getPlanName(), plan.getCredits());
                return plan.getCredits();
            }
            
            log.warn("[calculateMonthlyCredits][用户({})无法找到对应的套餐积分，订阅类型: {}, 计费周期: {}]", 
                userId, subscription.getSubscriptionType(), subscription.getBillingCycle());
            return 0;
            
        } catch (Exception e) {
            log.error("[calculateMonthlyCredits][计算用户({})月付积分失败]", userId, e);
            return 0;
        }
    }

    @Override
    public void invalidateUserCredits(Long userId) {
        // 获取用户的积分账户
        UserCreditsDO userCredits = userCreditsMapper.selectByUserId(userId);
        if (userCredits != null) {
            userCreditsMapper.deleteById(userCredits);
            log.info("[invalidateUserCredits][作废用户积分账户成功，用户ID: {}]", userId);
        }
    }

    @Override
    public void recordApiCall(Long userId, Integer featureType, String businessId, String callResult, String description) {
        log.info("[recordApiCall][记录API调用: 用户({}), 功能类型: {}, 调用结果: {}, 业务ID: {}]", 
            userId, featureType, callResult, businessId);
        
        try {
            // 获取用户当前积分（用于记录交易前后积分，但不实际扣减）
            UserCreditsDO userCredits = getUserCreditsByUserId(userId);
            if (userCredits == null) {
                userCredits = initUserCredits(userId);
            }
            
            // 根据调用结果选择交易类型
            TransactionTypeEnum transactionType;
            if ("failed".equals(callResult)) {
                transactionType = TransactionTypeEnum.API_CALL_FAILED;
            } else if ("no_data".equals(callResult)) {
                transactionType = TransactionTypeEnum.API_CALL_NO_DATA;
            } else {
                log.warn("[recordApiCall][未知的调用结果类型: {}]", callResult);
                return;
            }
            
            // 记录交易（积分数量为0，表示未消费积分）
            CreditsTransactionSaveReqVO transactionReqVO = new CreditsTransactionSaveReqVO();
            transactionReqVO.setUserId(userId);
            transactionReqVO.setCreditsAmount(0); // 未消费积分
            transactionReqVO.setTransactionType(transactionType.getCode());
            transactionReqVO.setBeforeCredits(userCredits.getRemainingCredits());
            transactionReqVO.setAfterCredits(userCredits.getRemainingCredits()); // 积分不变
            transactionReqVO.setBusinessType(featureType);
            transactionReqVO.setBusinessId(businessId);
            // 如果没有提供描述，则使用默认描述（包含功能名称）
            String finalDescription = description;
            if (finalDescription == null) {
                String featureName = getFeatureTypeName(featureType);
                finalDescription = String.format("%s - %s", transactionType.getDesc(), featureName);
            }
            transactionReqVO.setDescription(finalDescription);
            
            creditsTransactionService.createCreditsTransaction(transactionReqVO);
            
            log.info("[recordApiCall][API调用记录成功: 用户({}), 类型: {}]", userId, transactionType.getDesc());
            
        } catch (Exception e) {
            log.error("[recordApiCall][记录API调用失败: 用户({})]", userId, e);
        }
    }

    /**
     * 根据功能类型代码获取功能名称
     *
     * @param featureType 功能类型代码
     * @return 功能名称
     */
    private String getFeatureTypeName(Integer featureType) {
        try {
            FeatureTypeEnum featureTypeEnum = FeatureTypeEnum.valueOf(featureType);
            return featureTypeEnum.getName();
        } catch (Exception e) {
            log.warn("[getFeatureTypeName][未知的功能类型代码: {}]", featureType);
            return "未知功能";
        }
    }

}