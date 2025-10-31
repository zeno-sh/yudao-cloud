package cn.iocoder.yudao.module.dm.dal.mysql.plan;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.plan.ProductSelectionPlanDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.plan.vo.*;

/**
 * 选品计划 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ProductSelectionPlanMapper extends BaseMapperX<ProductSelectionPlanDO> {

    default PageResult<ProductSelectionPlanDO> selectPage(ProductSelectionPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductSelectionPlanDO>()
                .likeIfPresent(ProductSelectionPlanDO::getPlanName, reqVO.getPlanName())
                .likeIfPresent(ProductSelectionPlanDO::getPlanSkuId, reqVO.getPlanSkuId())
                .betweenIfPresent(ProductSelectionPlanDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(ProductSelectionPlanDO::getProductId, reqVO.getProductId())
                .orderByDesc(ProductSelectionPlanDO::getId));
    }

}