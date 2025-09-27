package cn.iocoder.yudao.module.dm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.*;

/**
 * 采购计划 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface PurchasePlanMapper extends BaseMapperX<PurchasePlanDO> {

    default PageResult<PurchasePlanDO> selectPage(PurchasePlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PurchasePlanDO>()
                .eqIfPresent(PurchasePlanDO::getBatchNumber, reqVO.getBatchNumber())
                .eqIfPresent(PurchasePlanDO::getRemark, reqVO.getRemark())
                .eqIfPresent(PurchasePlanDO::getAuditStatus, reqVO.getAuditStatus())
                .betweenIfPresent(PurchasePlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PurchasePlanDO::getId));
    }

}