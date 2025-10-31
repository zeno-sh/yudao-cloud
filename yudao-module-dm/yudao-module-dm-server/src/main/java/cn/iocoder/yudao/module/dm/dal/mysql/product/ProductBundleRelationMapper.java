package cn.iocoder.yudao.module.dm.dal.mysql.product;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductBundleRelationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 产品组合关系 Mapper
 *
 * @author zeno
 */
@Mapper
public interface ProductBundleRelationMapper extends BaseMapperX<ProductBundleRelationDO> {

    /**
     * 根据组合产品ID查询明细列表
     *
     * @param bundleProductId 组合产品ID
     * @return 明细列表
     */
    default List<ProductBundleRelationDO> selectListByBundleProductId(Long bundleProductId) {
        return selectList(ProductBundleRelationDO::getBundleProductId, bundleProductId);
    }

    /**
     * 根据组合产品ID删除明细
     *
     * @param bundleProductId 组合产品ID
     */
    default void deleteByBundleProductId(Long bundleProductId) {
        delete(ProductBundleRelationDO::getBundleProductId, bundleProductId);
    }
    
    /**
     * 根据子产品ID查询关联关系（用于级联查询）
     *
     * @param subProductId 子产品ID
     * @return 关联关系列表
     */
    default List<ProductBundleRelationDO> selectListBySubProductId(Long subProductId) {
        return selectList(ProductBundleRelationDO::getSubProductId, subProductId);
    }
}

