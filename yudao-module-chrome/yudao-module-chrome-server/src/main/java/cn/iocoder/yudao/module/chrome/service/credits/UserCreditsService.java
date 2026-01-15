package cn.iocoder.yudao.module.chrome.service.credits;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.chrome.controller.admin.credits.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.credits.UserCreditsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 用户积分账户 Service 接口
 *
 * @author Jax
 */
public interface UserCreditsService {

    /**
     * 创建用户积分账户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUserCredits(@Valid UserCreditsSaveReqVO createReqVO);

    /**
     * 更新用户积分账户
     *
     * @param updateReqVO 更新信息
     */
    void updateUserCredits(@Valid UserCreditsSaveReqVO updateReqVO);

    /**
     * 删除用户积分账户
     *
     * @param id 编号
     */
    void deleteUserCredits(Long id);

    /**
     * 获得用户积分账户
     *
     * @param id 编号
     * @return 用户积分账户
     */
    UserCreditsDO getUserCredits(Long id);

    /**
     * 获得用户积分账户分页
     *
     * @param pageReqVO 分页查询
     * @return 用户积分账户分页
     */
    PageResult<UserCreditsDO> getUserCreditsPage(UserCreditsPageReqVO pageReqVO);

    // ========== 以下是从 ChromeCreditsService 迁移的业务方法 ==========

    /**
     * 根据用户ID获取用户积分账户
     *
     * @param userId 用户ID
     * @return 积分账户信息
     */
    UserCreditsDO getUserCreditsByUserId(Long userId);

    /**
     * 初始化用户积分账户
     *
     * @param userId 用户ID
     * @return 积分账户信息
     */
    UserCreditsDO initUserCredits(Long userId);

    /**
     * 充值积分
     *
     * @param userId 用户ID
     * @param credits 充值积分数
     * @param businessId 业务ID（订单ID等）
     * @param description 描述
     * @return 是否成功
     */
    boolean rechargeCredits(Long userId, Integer credits, String businessId, String description);

    /**
     * 消费积分
     *
     * @param userId 用户ID
     * @param credits 消费积分数
     * @param featureType 功能类型
     * @param businessId 业务ID（使用记录ID等）
     * @return 是否成功
     */
    boolean consumeCredits(Long userId, Integer credits, Integer featureType, String businessId);

    /**
     * 检查积分余额是否足够
     *
     * @param userId 用户ID
     * @param requiredCredits 需要的积分数
     * @return 是否足够
     */
    boolean hasEnoughCredits(Long userId, Integer requiredCredits);

    /**
     * 重置用户积分（用于月付订阅的月度重置）
     *
     * @param userId 用户ID
     * @param newCredits 新的积分数
     * @return 是否成功
     */
    boolean resetCredits(Long userId, Integer newCredits);

    /**
     * 获取用户剩余积分
     *
     * @param userId 用户ID
     * @return 剩余积分数
     */
    Integer getRemainingCredits(Long userId);

    /**
     * 批量重置月付用户积分
     *
     * @return 重置的用户数量
     */
    int batchResetMonthlyUserCredits();

    /**
     * 作废用户的积分账户（设置deleted=true）
     *
     * @param userId 用户ID
     */
    void invalidateUserCredits(Long userId);

    /**
     * 记录API调用（不消费积分）
     *
     * @param userId 用户ID
     * @param featureType 功能类型
     * @param businessId 业务ID
     * @param callResult API调用结果（failed/no_data）
     * @param description 描述
     */
    void recordApiCall(Long userId, Integer featureType, String businessId, String callResult, String description);

}