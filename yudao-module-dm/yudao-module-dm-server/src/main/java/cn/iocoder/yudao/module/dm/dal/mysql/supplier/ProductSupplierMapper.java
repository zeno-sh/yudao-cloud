package cn.iocoder.yudao.module.dm.dal.mysql.supplier;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.supplier.ProductSupplierDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.supplier.vo.*;

/**
 * 供应商信息 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ProductSupplierMapper extends BaseMapperX<ProductSupplierDO> {

    default PageResult<ProductSupplierDO> selectPage(ProductSupplierPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductSupplierDO>()
                .likeIfPresent(ProductSupplierDO::getSupplierName, reqVO.getSupplierName())
                .eqIfPresent(ProductSupplierDO::getSupplierCode, reqVO.getSupplierCode())
                .eqIfPresent(ProductSupplierDO::getSourceFactory, reqVO.getSourceFactory())
                .orderByDesc(ProductSupplierDO::getId));
    }

}