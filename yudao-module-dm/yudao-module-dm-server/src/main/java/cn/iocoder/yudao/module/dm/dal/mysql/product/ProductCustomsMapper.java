package cn.iocoder.yudao.module.dm.dal.mysql.product;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductCustomsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 海关信息 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface ProductCustomsMapper extends BaseMapperX<ProductCustomsDO> {

    default ProductCustomsDO selectByProductId(Long productId) {
        return selectOne(ProductCustomsDO::getProductId, productId);
    }

    default int deleteByProductId(Long productId) {
        return delete(ProductCustomsDO::getProductId, productId);
    }

}