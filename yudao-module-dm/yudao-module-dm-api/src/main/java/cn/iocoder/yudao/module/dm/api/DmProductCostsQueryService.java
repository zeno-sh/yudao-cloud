package cn.iocoder.yudao.module.dm.api;

import cn.iocoder.yudao.module.dm.dto.ProductCostsDTO;

import java.util.Collection;
import java.util.List;

/**
 * 产品成本查询 API 接口
 *
 * @author Jax
 */
public interface DmProductCostsQueryService {

    /**
     * 根据产品ID获取产品成本信息
     *
     * @param productId 产品ID
     * @return 产品成本信息
     */
    ProductCostsDTO getProductCostsByProductId(Long productId);

    /**
     * 根据产品ID列表批量获取产品成本信息
     *
     * @param productIds 产品ID列表
     * @return 产品成本信息列表
     */
    List<ProductCostsDTO> batchGetProductCostsByProductIds(Collection<Long> productIds);

    /**
     * 根据平台和产品ID获取产品成本信息
     *
     * @param platform 目标平台
     * @param productId 产品ID
     * @return 产品成本信息
     */
    ProductCostsDTO getProductCostsByPlatformAndProductId(Integer platform, Long productId);

    /**
     * 根据平台和产品ID列表批量获取产品成本信息
     *
     * @param platform 目标平台
     * @param productIds 产品ID列表
     * @return 产品成本信息列表
     */
    List<ProductCostsDTO> batchGetProductCostsByPlatformAndProductIds(Integer platform, Collection<Long> productIds);

} 