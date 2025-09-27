package cn.iocoder.yudao.module.dm.dal.mysql.product;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPlatformTrendDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品销量趋势 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ProductPlatformTrendMapper extends BaseMapperX<ProductPlatformTrendDO> {

    default List<ProductPlatformTrendDO> selectListByProductId(Long productId) {
        return selectList(ProductPlatformTrendDO::getProductId, productId);
    }

    default int deleteByProductId(Long productId) {
        return delete(ProductPlatformTrendDO::getProductId, productId);
    }

    default List<ProductPlatformTrendDO> batchQueryByProductIds(List<Long> productIds) {
        return selectList(ProductPlatformTrendDO::getProductId, productIds);
    }
}