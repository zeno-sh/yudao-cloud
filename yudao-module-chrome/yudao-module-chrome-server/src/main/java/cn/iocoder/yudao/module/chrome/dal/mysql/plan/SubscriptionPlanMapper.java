package cn.iocoder.yudao.module.chrome.dal.mysql.plan;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.plan.SubscriptionPlanDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.chrome.controller.admin.plan.vo.*;

/**
 * 订阅套餐配置 Mapper
 *
 * @author Jax
 */
@Mapper
public interface SubscriptionPlanMapper extends BaseMapperX<SubscriptionPlanDO> {

    default PageResult<SubscriptionPlanDO> selectPage(SubscriptionPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SubscriptionPlanDO>()
                .likeIfPresent(SubscriptionPlanDO::getPlanName, reqVO.getPlanName())
                .eqIfPresent(SubscriptionPlanDO::getSubscriptionType, reqVO.getSubscriptionType())
                .eqIfPresent(SubscriptionPlanDO::getBillingCycle, reqVO.getBillingCycle())
                .eqIfPresent(SubscriptionPlanDO::getCredits, reqVO.getCredits())
                .eqIfPresent(SubscriptionPlanDO::getPrice, reqVO.getPrice())
                .eqIfPresent(SubscriptionPlanDO::getCurrency, reqVO.getCurrency())
                .eqIfPresent(SubscriptionPlanDO::getStatus, reqVO.getStatus())
                .eqIfPresent(SubscriptionPlanDO::getSortOrder, reqVO.getSortOrder())
                .eqIfPresent(SubscriptionPlanDO::getDescription, reqVO.getDescription())
                .betweenIfPresent(SubscriptionPlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SubscriptionPlanDO::getId));
    }

}