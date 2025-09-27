package cn.iocoder.yudao.module.dm.dal.mysql.product;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 采购信息 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ProductPurchaseMapper extends BaseMapperX<ProductPurchaseDO> {

    default List<ProductPurchaseDO> selectListByProductId(Long productId) {
        return selectList(ProductPurchaseDO::getProductId, productId);
    }

    default int deleteByProductId(Long productId) {
        return delete(ProductPurchaseDO::getProductId, productId);
    }

    default List<ProductPurchaseDO> batchQueryByProductIds(List<Long> productIds) {
        return selectList("product_id", productIds);
    }
}