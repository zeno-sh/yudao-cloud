package cn.iocoder.yudao.module.dm.rpc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.dm.api.DmProductCostsQueryService;
import cn.iocoder.yudao.module.dm.dto.ProductCostsDTO;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.service.productcosts.ProductCostsService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
public class DmProductCostsQueryServiceImpl implements DmProductCostsQueryService {

    @Resource
    private ProductCostsService productCostsService;

    @Override
    public CommonResult<ProductCostsDTO> getProductCostsByProductId(Long productId) {
        if (productId == null) {
            return CommonResult.success(null);
        }
        
        List<ProductCostsDO> productCostsList = productCostsService.getProductCostsListByProductId(productId);
        if (productCostsList == null || productCostsList.isEmpty()) {
            return CommonResult.success(null);
        }
        
        // 取第一个成本记录（如果有多个的话）
        return CommonResult.success(convertToDTO(productCostsList.get(0)));
    }

    @Override
    public CommonResult<List<ProductCostsDTO>> batchGetProductCostsByProductIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return CommonResult.success(Collections.emptyList());
        }
        
        List<ProductCostsDO> productCostsList = productCostsService.batchProductCostsListByProductIds(productIds);
        if (productCostsList == null || productCostsList.isEmpty()) {
            return CommonResult.success(Collections.emptyList());
        }
        
        List<ProductCostsDTO> result = productCostsList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return CommonResult.success(result);
    }

    @Override
    public CommonResult<ProductCostsDTO> getProductCostsByPlatformAndProductId(Integer platform, Long productId) {
        if (platform == null || productId == null) {
            return CommonResult.success(null);
        }
        
        List<ProductCostsDO> productCostsList = productCostsService.getProductCostsListByProductId(productId);
        if (productCostsList == null || productCostsList.isEmpty()) {
            return CommonResult.success(null);
        }
        
        // 根据平台过滤
        ProductCostsDO productCosts = productCostsList.stream()
                .filter(costs -> platform.equals(costs.getPlatform()))
                .findFirst()
                .orElse(null);
                
        return CommonResult.success(productCosts != null ? convertToDTO(productCosts) : null);
    }

    @Override
    public CommonResult<List<ProductCostsDTO>> batchGetProductCostsByPlatformAndProductIds(Integer platform, Collection<Long> productIds) {
        if (platform == null || productIds == null || productIds.isEmpty()) {
            return CommonResult.success(Collections.emptyList());
        }
        
        List<ProductCostsDO> productCostsList = productCostsService.batchProductCostsListByProductIds(productIds);
        if (productCostsList == null || productCostsList.isEmpty()) {
            return CommonResult.success(Collections.emptyList());
        }
        
        // 根据平台过滤
        List<ProductCostsDTO> result = productCostsList.stream()
                .filter(costs -> platform.equals(costs.getPlatform()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return CommonResult.success(result);
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