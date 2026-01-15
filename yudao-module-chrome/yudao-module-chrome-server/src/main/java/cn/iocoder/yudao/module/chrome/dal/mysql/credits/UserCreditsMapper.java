package cn.iocoder.yudao.module.chrome.dal.mysql.credits;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.credits.UserCreditsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import cn.iocoder.yudao.module.chrome.controller.admin.credits.vo.*;

/**
 * 用户积分账户 Mapper
 *
 * @author Jax
 */
@Mapper
public interface UserCreditsMapper extends BaseMapperX<UserCreditsDO> {

    default PageResult<UserCreditsDO> selectPage(UserCreditsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserCreditsDO>()
                .eqIfPresent(UserCreditsDO::getUserId, reqVO.getUserId())
                .eqIfPresent(UserCreditsDO::getTotalCredits, reqVO.getTotalCredits())
                .eqIfPresent(UserCreditsDO::getUsedCredits, reqVO.getUsedCredits())
                .eqIfPresent(UserCreditsDO::getRemainingCredits, reqVO.getRemainingCredits())
                .betweenIfPresent(UserCreditsDO::getLastResetTime, reqVO.getLastResetTime())
                .betweenIfPresent(UserCreditsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UserCreditsDO::getId));
    }

    /**
     * 根据用户ID查询积分账户
     *
     * @param userId 用户ID
     * @return 积分账户
     */
    default UserCreditsDO selectByUserId(Long userId) {
        return selectOne(UserCreditsDO::getUserId, userId);
    }

    /**
     * 查询所有免费版用户的积分账户
     * 这里假设免费版用户的总积分<=10
     *
     * @return 免费版用户积分账户列表
     */
    default List<UserCreditsDO> selectFreeUserCredits() {
        return selectList(new LambdaQueryWrapperX<UserCreditsDO>()
                .le(UserCreditsDO::getTotalCredits, 10));
    }

    /**
     * 查询需要重置积分的月付用户
     * 基于订阅表的购买时间计算，找出今天应该重置的用户
     *
     * @return 需要重置的用户ID列表
     */
    @Select("SELECT user_id FROM chrome_subscription " +
            "WHERE status = 1 " +
            "AND deleted = 0 " +
            "AND end_time > NOW() " +
            "AND billing_cycle = 10 " +
            "AND DAY(start_time) = DAY(NOW()) " +
            "AND subscription_type IN (20, 30)")
    List<Long> selectMonthlyUserIdsForReset();

    /**
     * 根据用户ID列表查询积分账户
     *
     * @param userIds 用户ID列表
     * @return 用户积分账户列表
     */
    default List<UserCreditsDO> selectByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return selectList(new LambdaQueryWrapperX<UserCreditsDO>()
                .in(UserCreditsDO::getUserId, userIds));
    }

}