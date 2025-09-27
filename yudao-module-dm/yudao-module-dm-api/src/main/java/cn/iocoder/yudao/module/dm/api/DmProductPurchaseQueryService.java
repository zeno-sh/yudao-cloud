package cn.iocoder.yudao.module.dm.api;

import cn.iocoder.yudao.module.dm.dto.ProductPurchaseDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 产品采购信息查询 API 接口
 *
 * @author Jax
 */
public interface DmProductPurchaseQueryService {

    /**
     * 根据产品ID获取产品采购信息
     *
     * @param productId 产品ID
     * @return 产品采购信息
     */
    ProductPurchaseDTO getProductPurchaseByProductId(Long productId);

    /**
     * 根据产品ID列表批量获取产品采购信息
     *
     * @param productIds 产品ID列表
     * @return 产品采购信息Map，key为产品ID，value为采购信息
     */
    Map<Long, ProductPurchaseDTO> batchGetProductPurchaseByProductIds(Collection<Long> productIds);
} 