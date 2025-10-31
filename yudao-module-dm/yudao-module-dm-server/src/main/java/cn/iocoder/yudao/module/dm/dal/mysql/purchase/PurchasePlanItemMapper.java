package cn.iocoder.yudao.module.dm.dal.mysql.purchase;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.PurchasePlanItemPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购计划详情 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface PurchasePlanItemMapper extends BaseMapperX<PurchasePlanItemDO> {

    default List<PurchasePlanItemDO> selectListByPlanId(Long planId) {
        return selectList(PurchasePlanItemDO::getPlanId, planId);
    }

    default int deleteByPlanId(Long planId) {
        return delete(PurchasePlanItemDO::getPlanId, planId);
    }

    /**
     * 采购计划详情 Mapper
     *
     * @author Zeno
     */
    @Mapper

    default PageResult<PurchasePlanItemDO> selectPage(PurchasePlanItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PurchasePlanItemDO>()
                .eqIfPresent(PurchasePlanItemDO::getPlanNumber, reqVO.getPlanNumber())
                .eqIfPresent(PurchasePlanItemDO::getPlanId, reqVO.getPlanId())
                .eqIfPresent(PurchasePlanItemDO::getProductId, reqVO.getProductId())
                .inIfPresent(PurchasePlanItemDO::getProductId, reqVO.getProductIds())
                .betweenIfPresent(PurchasePlanItemDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(PurchasePlanItemDO::getStatus, reqVO.getStatus())
                .eqIfPresent(PurchasePlanItemDO::getAuditStatus, reqVO.getAuditStatus())
                .orderByDesc(PurchasePlanItemDO::getProductId));
    }
}