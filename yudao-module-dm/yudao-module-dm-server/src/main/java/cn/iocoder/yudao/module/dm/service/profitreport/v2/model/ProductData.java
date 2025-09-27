package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 产品数据
 *
 * @author Jax
 */
@Data
@Builder
public class ProductData {
    
    /**
     * 在线产品列表
     */
    private List<OzonProductOnlineDO> onlineProducts;
    
    /**
     * 产品基础信息列表
     */
    private List<ProductInfoDO> productInfos;
    
    /**
     * 产品采购信息列表
     */
    private List<ProductPurchaseDO> productPurchases;
    
    /**
     * 产品ID到基础信息的映射
     */
    private Map<Long, ProductInfoDO> productInfoMap;
    
    /**
     * 产品ID到采购信息的映射
     */
    private Map<Long, ProductPurchaseDO> productPurchaseMap;
    
    /**
     * SKU到产品的映射
     */
    private Map<String, OzonProductOnlineDO> skuToProductMap;
    
    /**
     * 平台SKU ID到产品ID的映射
     */
    private Map<String, Long> platformSkuIdMapping;
    
    /**
     * 产品总数
     */
    private Integer totalProductCount;
    
    /**
     * 获取产品采购信息映射
     */
    public Map<Long, ProductPurchaseDO> getProductPurchases() {
        return productPurchaseMap;
    }
    
    /**
     * 获取平台SKU ID映射
     */
    public Map<String, Long> getPlatformSkuIdMapping() {
        return platformSkuIdMapping;
    }
} 