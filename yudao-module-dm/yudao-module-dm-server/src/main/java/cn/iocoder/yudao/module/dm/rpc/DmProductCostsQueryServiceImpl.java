package cn.iocoder.yudao.module.dm.rpc;

import cn.iocoder.yudao.module.dm.api.DmProductCostsQueryService;
import cn.iocoder.yudao.module.dm.dto.ProductCostsDTO;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.service.productcosts.ProductCostsService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品成本查询 API 接口实现类
 *
 * @author Jax
 */
@Service
public class DmProductCostsQueryServiceImpl implements DmProductCostsQueryService {

    @Resource
    private ProductCostsService productCostsService;

    @Override
    public ProductCostsDTO getProductCostsByProductId(Long productId) {
        if (productId == null) {
            return null;
        }
        
        List<ProductCostsDO> productCostsList = productCostsService.getProductCostsListByProductId(productId);
        if (productCostsList == null || productCostsList.isEmpty()) {
            return null;
        }
        
        // 取第一个成本记录（如果有多个的话）
        return convertToDTO(productCostsList.get(0));
    }

    @Override
    public List<ProductCostsDTO> batchGetProductCostsByProductIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<ProductCostsDO> productCostsList = productCostsService.batchProductCostsListByProductIds(productIds);
        if (productCostsList == null || productCostsList.isEmpty()) {
            return Collections.emptyList();
        }
        
        return productCostsList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductCostsDTO getProductCostsByPlatformAndProductId(Integer platform, Long productId) {
        if (platform == null || productId == null) {
            return null;
        }
        
        List<ProductCostsDO> productCostsList = productCostsService.getProductCostsListByProductId(productId);
        if (productCostsList == null || productCostsList.isEmpty()) {
            return null;
        }
        
        // 根据平台过滤
        ProductCostsDO productCosts = productCostsList.stream()
                .filter(costs -> platform.equals(costs.getPlatform()))
                .findFirst()
                .orElse(null);
                
        return productCosts != null ? convertToDTO(productCosts) : null;
    }

    @Override
    public List<ProductCostsDTO> batchGetProductCostsByPlatformAndProductIds(Integer platform, Collection<Long> productIds) {
        if (platform == null || productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<ProductCostsDO> productCostsList = productCostsService.batchProductCostsListByProductIds(productIds);
        if (productCostsList == null || productCostsList.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 根据平台过滤
        return productCostsList.stream()
                .filter(costs -> platform.equals(costs.getPlatform()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将 ProductCostsDO 转换为 ProductCostsDTO
     *
     * @param productCostsDO 数据对象
     * @return DTO对象
     */
    private ProductCostsDTO convertToDTO(ProductCostsDO productCostsDO) {
        if (productCostsDO == null) {
            return null;
        }
        
        ProductCostsDTO dto = new ProductCostsDTO();
        BeanUtils.copyProperties(productCostsDO, dto);
        return dto;
    }

} 