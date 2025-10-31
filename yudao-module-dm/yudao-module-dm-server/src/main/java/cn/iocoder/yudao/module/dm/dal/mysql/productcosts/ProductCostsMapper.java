package cn.iocoder.yudao.module.dm.dal.mysql.productcosts;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.productcosts.vo.*;

/**
 * 产品成本结构 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ProductCostsMapper extends BaseMapperX<ProductCostsDO> {

    default PageResult<ProductCostsDO> selectPage(ProductCostsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductCostsDO>()
                .eqIfPresent(ProductCostsDO::getProductId, reqVO.getProductId())
                .betweenIfPresent(ProductCostsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProductCostsDO::getId));
    }

}