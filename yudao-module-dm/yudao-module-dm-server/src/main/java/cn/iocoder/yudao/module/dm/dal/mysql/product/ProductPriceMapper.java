package cn.iocoder.yudao.module.dm.dal.mysql.product;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPriceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品价格策略 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ProductPriceMapper extends BaseMapperX<ProductPriceDO> {

    default List<ProductPriceDO> selectListByProductId(Long productId) {
        return selectList(ProductPriceDO::getProductId, productId);
    }

    default int deleteByProductId(Long productId) {
        return delete(ProductPriceDO::getProductId, productId);
    }

    default List<ProductPriceDO> batchQueryByIds(List<Long> ids) {
        return selectList(ProductPriceDO::getProductId, ids);
    }
}