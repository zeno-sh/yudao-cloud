package cn.iocoder.yudao.module.chrome.dal.mysql.subscription;

import java.util.*;
import java.time.LocalDateTime;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.subscription.SubscriptionDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.chrome.controller.admin.subscription.vo.*;

/**
 * 插件订阅 Mapper
 *
 * @author Jax
 */
@Mapper
public interface SubscriptionMapper extends BaseMapperX<SubscriptionDO> {

    default PageResult<SubscriptionDO> selectPage(SubscriptionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SubscriptionDO>()
                .eqIfPresent(SubscriptionDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SubscriptionDO::getSubscriptionType, reqVO.getSubscriptionType())
                .eqIfPresent(SubscriptionDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SubscriptionDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(SubscriptionDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(SubscriptionDO::getPaymentDuration, reqVO.getPaymentDuration())
                .eqIfPresent(SubscriptionDO::getAutoRenew, reqVO.getAutoRenew())
                .betweenIfPresent(SubscriptionDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SubscriptionDO::getId));
    }

    default SubscriptionDO selectActiveByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapperX<SubscriptionDO>()
                .eq(SubscriptionDO::getUserId, userId)
                .eq(SubscriptionDO::getStatus, 1) // 1表示有效状态
                .gt(SubscriptionDO::getEndTime, LocalDateTime.now())
                .orderByDesc(SubscriptionDO::getEndTime)
                .last("LIMIT 1"));
    }

    default List<SubscriptionDO> selectExpiringSubscriptions(LocalDateTime expireTime) {
        return selectList(new LambdaQueryWrapperX<SubscriptionDO>()
                .eq(SubscriptionDO::getStatus, 1) // 1表示有效状态
                .le(SubscriptionDO::getEndTime, expireTime)
                .gt(SubscriptionDO::getEndTime, LocalDateTime.now())
                .orderByAsc(SubscriptionDO::getEndTime));
    }

    default List<SubscriptionDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<SubscriptionDO>()
                .eq(SubscriptionDO::getUserId, userId));
    }

}