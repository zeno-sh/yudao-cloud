package cn.iocoder.yudao.module.chrome.service.subscription;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.chrome.controller.admin.subscription.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 插件订阅 Service 接口
 *
 * @author Jax
 */
public interface SubscriptionService {

    /**
     * 创建插件订阅
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSubscription(@Valid SubscriptionSaveReqVO createReqVO);

    /**
     * 更新插件订阅
     *
     * @param updateReqVO 更新信息
     */
    void updateSubscription(@Valid SubscriptionSaveReqVO updateReqVO);

    /**
     * 删除插件订阅
     *
     * @param id 编号
     */
    void deleteSubscription(Long id);

    /**
     * 获得插件订阅
     *
     * @param id 编号
     * @return 插件订阅
     */
    SubscriptionDO getSubscription(Long id);

    /**
     * 获得插件订阅分页
     *
     * @param pageReqVO 分页查询
     * @return 插件订阅分页
     */
    PageResult<SubscriptionDO> getSubscriptionPage(SubscriptionPageReqVO pageReqVO);

    /**
     * 根据用户ID获得有效的订阅
     *
     * @param userId 用户ID
     * @return 有效的订阅
     */
    SubscriptionDO getActiveSubscriptionByUserId(Long userId);

    /**
     * 升级订阅
     *
     * @param userId 用户ID
     * @param newSubscriptionType 新订阅类型
     * @param paymentDuration 付费时长（天数）
     * @return 升级后的订阅ID
     */
    Long upgradeSubscription(Long userId, Integer newSubscriptionType, Integer paymentDuration);
    
    /**
     * 升级订阅（带planId）
     *
     * @param userId 用户ID
     * @param newSubscriptionType 新订阅类型
     * @param paymentDuration 付费时长（天数）
     * @param planId 套餐ID
     * @return 升级后的订阅ID
     */
    Long upgradeSubscription(Long userId, Integer newSubscriptionType, Integer paymentDuration, Long planId);

    /**
     * 续费订阅
     *
     * @param userId 用户ID
     * @param paymentDuration 续费时长（天数）
     * @return 续费后的订阅ID
     */
    Long renewSubscription(Long userId, Integer paymentDuration);

    /**
     * 校验订阅状态
     *
     * @param userId 用户ID
     * @return 是否有有效订阅
     */
    boolean validateSubscriptionStatus(Long userId);

    /**
     * 获取用户订阅统计信息
     *
     * @param userId 用户ID
     * @return 订阅统计信息
     */
    Map<String, Object> getSubscriptionStats(Long userId);

    /**
     * 自动续费处理
     *
     * @param userId 用户ID
     * @return 是否续费成功
     */
    boolean processAutoRenewal(Long userId);

    /**
     * 获取即将过期的订阅列表
     *
     * @param days 提前天数
     * @return 即将过期的订阅列表
     */
    List<SubscriptionDO> getExpiringSubscriptions(int days);

    /**
     * 订阅到期降级处理（绕过正常的升级/降级权限检查）
     * 专门用于定时任务处理订阅到期时的降级操作
     *
     * @param userId 用户ID
     * @param targetSubscriptionType 目标订阅类型（通常是免费版）
     * @param duration 有效期天数
     * @return 降级后的订阅ID
     */
    Long downgradeExpiredSubscription(Long userId, Integer targetSubscriptionType, Integer duration);

    /**
     * 作废用户的所有订阅（设置deleted=true）
     *
     * @param userId 用户ID
     */
    void invalidateUserSubscriptions(Long userId);

}