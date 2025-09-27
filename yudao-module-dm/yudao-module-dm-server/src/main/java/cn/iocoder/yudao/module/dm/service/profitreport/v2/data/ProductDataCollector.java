package cn.iocoder.yudao.module.dm.service.profitreport.v2.data;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitCalculationRequestVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.service.online.OzonProductOnlineService;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.ProductData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品数据收集器
 * 负责收集产品信息和在线产品数据
 *
 * @author Jax
 */
@Component
@Slf4j
public class ProductDataCollector {
    
    @Resource
    private OzonProductOnlineService ozonProductOnlineService;
    @Resource
    private ProductInfoService productInfoService;
    
    /**
     * 收集产品数据
     *
     * @param request 计算请求参数
     * @param taskId 任务ID
     * @return 产品数据
     */
    public ProductData collect(ProfitCalculationRequestVO request, String taskId) {
        // 只取第一个clientId
        String clientId = (request.getClientIds() != null && request.getClientIds().length > 0) 
                ? request.getClientIds()[0] : null;
        
        log.info("开始收集产品数据: clientId={}, taskId={}", clientId, taskId);
        
        if (clientId == null) {
            throw new IllegalArgumentException("clientId不能为空");
        }
        
        try {
            // 收集该门店的在线产品数据
            List<OzonProductOnlineDO> onlineProducts = ozonProductOnlineService.getAllProductOnlineByClientId(clientId);
            log.info("收集到在线产品数据: {} 条, taskId={}", onlineProducts.size(), taskId);
            
            // 收集对应的产品基础信息
            List<Long> productIds = onlineProducts.stream()
                    .map(OzonProductOnlineDO::getDmProductId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<ProductInfoDO> productInfos = productInfoService.batchQueryProductInfoList(productIds);
            log.info("收集到产品基础信息: {} 条, taskId={}", productInfos.size(), taskId);
            
            // 收集产品采购信息
            Map<Long, ProductPurchaseDO> productPurchaseMap = Collections.emptyMap();
            if (!productIds.isEmpty()) {
                productPurchaseMap = productInfoService.batchProductPurchaseListByProductIds(
                        productIds.toArray(new Long[0]));
            }
            log.info("收集到产品采购信息: {} 条, taskId={}", productPurchaseMap.size(), taskId);
            
            // 构建产品ID到信息的映射
            Map<Long, ProductInfoDO> productInfoMap = productInfos.stream()
                    .collect(Collectors.toMap(ProductInfoDO::getId, p -> p, (p1, p2) -> p1));
            
            // 构建SKU到产品映射
            Map<String, OzonProductOnlineDO> skuToProductMap = onlineProducts.stream()
                    .collect(Collectors.toMap(
                            p -> p.getPlatformSkuId(), 
                            p -> p, 
                            (p1, p2) -> p1));
            
            // 构建平台SKU ID到产品ID的映射
            Map<String, Long> platformSkuIdMapping = onlineProducts.stream()
                    .filter(p -> p.getPlatformSkuId() != null && p.getDmProductId() != null)
                    .collect(Collectors.toMap(
                            OzonProductOnlineDO::getPlatformSkuId,
                            OzonProductOnlineDO::getDmProductId,
                            (p1, p2) -> p1));
            
            log.info("构建平台SKU映射关系: {} 个, taskId={}", platformSkuIdMapping.size(), taskId);
            
            return ProductData.builder()
                    .onlineProducts(onlineProducts)
                    .productInfos(productInfos)
                    .productPurchases(productPurchaseMap.values().stream().collect(Collectors.toList()))
                    .productInfoMap(productInfoMap)
                    .productPurchaseMap(productPurchaseMap)
                    .skuToProductMap(skuToProductMap)
                    .platformSkuIdMapping(platformSkuIdMapping)
                    .totalProductCount(onlineProducts.size())
                    .build();
                    
        } catch (Exception e) {
            log.error("收集产品数据失败: clientId={}, taskId={}", clientId, taskId, e);
            throw new RuntimeException("产品数据收集失败", e);
        }
    }
} 