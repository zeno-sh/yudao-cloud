package cn.iocoder.yudao.module.dm.dal.mysql.product;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.SupplierPriceOfferDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商报价 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface SupplierPriceOfferMapper extends BaseMapperX<SupplierPriceOfferDO> {

    default List<SupplierPriceOfferDO> selectListByProductId(Long productId) {
        return selectList(SupplierPriceOfferDO::getProductId, productId);
    }

    default int deleteByProductId(Long productId) {
        return delete(SupplierPriceOfferDO::getProductId, productId);
    }

}