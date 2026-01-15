package cn.iocoder.yudao.module.chrome.service.permission;

import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.usage.UsageRecordDO;
import cn.iocoder.yudao.module.chrome.enums.FeatureTypeEnum;
import cn.iocoder.yudao.module.chrome.enums.SubscriptionTypeEnum;

/**
 * Chrome权限校验 Service 接口
 *
 * @author Jax
 */
public interface ChromePermissionService {

    /**
     * 校验用户功能权限
     *
     * @param userId 用户ID
     * @param featureType 功能类型
     * @return 是否有权限
     */
    boolean validateFeaturePermission(Long userId, FeatureTypeEnum featureType);

    /**
     * 校验用户功能权限
     *
     * @param userId 用户ID
     * @param featureType 功能类型
     * @param requiredCredits 需要的积分数量
     * @return 是否有权限
     */
    boolean validateFeaturePermission(Long userId, FeatureTypeEnum featureType, Integer requiredCredits);

    /**
     * 校验用户订阅状态
     *
     * @param userId 用户ID
     * @return 订阅信息
     */
    SubscriptionDO validateSubscriptionStatus(Long userId);

    /**
     * 校验使用次数限制
     *
     * @param userId 用户ID
     * @param featureType 功能类型
     * @return 是否超出限制
     */
    boolean validateUsageLimit(Long userId, FeatureTypeEnum featureType);

    /**
     * 记录功能使用情况
     *
     * @param userId 用户ID
     * @param featureType 功能类型
     */
    void recordUsage(Long userId, FeatureTypeEnum featureType);

    /**
     * 获取用户今日使用次数
     *
     * @param userId 用户ID
     * @param featureType 功能类型
     * @return 使用次数
     */
    int getTodayUsage(Long userId, FeatureTypeEnum featureType);

    /**
     * 获取功能使用限制
     *
     * @param subscriptionType 订阅类型
     * @param featureType 功能类型
     * @return 使用限制
     */
    int getFeatureLimit(SubscriptionTypeEnum subscriptionType, FeatureTypeEnum featureType);

    /**
     * 检查功能是否允许访问
     *
     * @param subscriptionType 订阅类型
     * @param featureType 功能类型
     * @return 是否允许
     */
    boolean hasFeaturePermission(SubscriptionTypeEnum subscriptionType, FeatureTypeEnum featureType);
}