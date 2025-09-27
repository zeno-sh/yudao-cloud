package cn.iocoder.yudao.module.dm.rpc;

import cn.iocoder.yudao.module.dm.api.DmProductPurchaseQueryService;
import cn.iocoder.yudao.module.dm.dto.ProductPurchaseDTO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品采购信息查询 API 接口实现类
 *
 * @author Jax
 */
@Service
public class DmProductPurchaseQueryServiceImpl implements DmProductPurchaseQueryService {

    @Resource
    private ProductInfoService productInfoService;

    @Override
    public ProductPurchaseDTO getProductPurchaseByProductId(Long productId) {
        if (productId == null) {
            return null;
        }
        
        List<ProductPurchaseDO> productPurchaseList = productInfoService.getProductPurchaseListByProductId(productId);
        if (productPurchaseList == null || productPurchaseList.isEmpty()) {
            return null;
        }
        
        // 优先选择首选的采购信息，如果没有首选的则取第一个
        ProductPurchaseDO productPurchase = productPurchaseList.stream()
                .filter(purchase -> "Y".equals(purchase.getFirstChoice()))
                .findFirst()
                .orElse(productPurchaseList.get(0));
        
        return convertToDTO(productPurchase);
    }

    @Override
    public Map<Long, ProductPurchaseDTO> batchGetProductPurchaseByProductIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<Long, ProductPurchaseDO> productPurchaseMap = productInfoService.batchProductPurchaseListByProductIds(
                productIds.toArray(new Long[0]));
        
        if (productPurchaseMap == null || productPurchaseMap.isEmpty()) {
            return Collections.emptyMap();
        }
        
        return productPurchaseMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> convertToDTO(entry.getValue())
                ));
    }

    /**
     * 转换为DTO
     * 
     * @param productPurchase 产品采购信息DO
     * @return 产品采购信息DTO
     */
    private ProductPurchaseDTO convertToDTO(ProductPurchaseDO productPurchase) {
        if (productPurchase == null) {
            return null;
        }
        
        ProductPurchaseDTO dto = new ProductPurchaseDTO();
        BeanUtils.copyProperties(productPurchase, dto);
        return dto;
    }
} 