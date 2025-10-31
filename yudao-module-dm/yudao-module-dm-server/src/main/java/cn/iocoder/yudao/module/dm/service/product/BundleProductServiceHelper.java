package cn.iocoder.yudao.module.dm.service.product;

import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductBundleItemReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductInfoSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductBundleRelationDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductBundleRelationMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 组合产品业务逻辑辅助类
 * 
 * 包含组合产品相关的核心业务方法
 * 需要在 ProductInfoServiceImpl 中调用这些方法
 *
 * @author zeno
 */
@Component
@Slf4j
public class BundleProductServiceHelper {

    @Resource
    private ProductInfoMapper productInfoMapper;
    
    @Resource
    private ProductBundleRelationMapper bundleRelationMapper;

    /**
     * 校验组合产品数据
     */
    public void validateBundleProduct(ProductInfoSaveReqVO reqVO) {
        // 1. 校验组合类型
        if (reqVO.getBundleType() == null) {
            throw exception(BUNDLE_TYPE_REQUIRED);
        }
        
        // 2. 自定义模式时，必须传入 costPrice
        if (reqVO.getBundleType() == 1 && reqVO.getCostPrice() == null) {
            throw exception(COST_PRICE_REQUIRED);
        }
        
        // 3. 校验明细列表
        if (CollectionUtils.isEmpty(reqVO.getBundleItems())) {
            throw exception(BUNDLE_ITEMS_REQUIRED);
        }
        
        // 4. 校验子产品是否存在
        for (ProductBundleItemReqVO item : reqVO.getBundleItems()) {
            ProductInfoDO subProduct = productInfoMapper.selectById(item.getSubProductId());
            if (subProduct == null) {
                throw exception(SUB_PRODUCT_NOT_EXISTS, item.getSubProductId());
            }
            
            // 5. 防止组合产品嵌套（可选）
            if (subProduct.getProductType() != null && subProduct.getProductType() == 1) {
                throw exception(BUNDLE_PRODUCT_CANNOT_NEST);
            }
        }
    }

    /**
     * 创建组合关系（精简版：只存储关联关系）
     */
    public List<ProductBundleRelationDO> createBundleRelations(Long bundleProductId, 
                                                               List<ProductBundleItemReqVO> itemReqVOs) {
        List<ProductBundleRelationDO> relations = new ArrayList<>();
        
        for (ProductBundleItemReqVO itemReqVO : itemReqVOs) {
            // 创建关系（只存储关联信息，不存储冗余数据）
            ProductBundleRelationDO relation = ProductBundleRelationDO.builder()
                    .bundleProductId(bundleProductId)
                    .subProductId(itemReqVO.getSubProductId())
                    .quantity(itemReqVO.getQuantity())
                    .sortOrder(itemReqVO.getSortOrder() != null ? itemReqVO.getSortOrder() : 0)
                    .remark(itemReqVO.getRemark())
                    .build();
            
            relations.add(relation);
        }
        
        return relations;
    }

    /**
     * 计算组合产品成本价
     * 
     * @param bundleType 组合类型：1=自定义 2=自动累计
     * @param userInputCostPrice 用户输入的成本价（bundleType=1时使用）
     * @param relations 组合产品明细
     * @return 最终成本价
     */
    public BigDecimal calculateBundleCostPrice(Integer bundleType, 
                                              BigDecimal userInputCostPrice, 
                                              List<ProductBundleRelationDO> relations) {
        if (bundleType == 1) {
            // 自定义成本价模式：直接使用用户输入的值
            return userInputCostPrice;
        } else {
            // 自动累计模式：实时查询子产品成本价并累加
            BigDecimal totalCost = BigDecimal.ZERO;
            
            for (ProductBundleRelationDO relation : relations) {
                // 实时获取子产品信息
                ProductInfoDO subProduct = productInfoMapper.selectById(relation.getSubProductId());
                if (subProduct != null && subProduct.getCostPrice() != null) {
                    // 计算该项成本：单价 * 数量
                    BigDecimal itemCost = subProduct.getCostPrice()
                            .multiply(new BigDecimal(relation.getQuantity()));
                    totalCost = totalCost.add(itemCost);
                }
            }
            
            return totalCost;
        }
    }

    /**
     * 重新计算组合产品成本价（仅自动累计模式）
     */
    @Transactional(rollbackFor = Exception.class)
    public void recalculateBundleCostPrice(Long bundleProductId) {
        // 1. 获取产品
        ProductInfoDO product = productInfoMapper.selectById(bundleProductId);
        if (product == null || product.getProductType() != 1) {
            throw exception(PRODUCT_NOT_BUNDLE);
        }
        
        // 2. 仅自动累计模式需要重算
        if (product.getBundleType() != 2) {
            log.warn("[recalculateBundleCostPrice] 产品{}为自定义成本价模式，跳过", bundleProductId);
            return;
        }
        
        // 3. 获取关联关系
        List<ProductBundleRelationDO> relations = bundleRelationMapper
                .selectListByBundleProductId(bundleProductId);
        
        // 4. 实时查询子产品成本价并计算总成本
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (ProductBundleRelationDO relation : relations) {
            // 实时获取子产品信息（不存储快照）
            ProductInfoDO subProduct = productInfoMapper.selectById(relation.getSubProductId());
            if (subProduct != null && subProduct.getCostPrice() != null) {
                // 计算该项成本：单价 * 数量
                BigDecimal itemCost = subProduct.getCostPrice()
                        .multiply(new BigDecimal(relation.getQuantity()));
                totalCost = totalCost.add(itemCost);
            }
        }
        
        // 5. 更新产品成本价
        ProductInfoDO updateObj = new ProductInfoDO();
        updateObj.setId(bundleProductId);
        updateObj.setCostPrice(totalCost);
        productInfoMapper.updateById(updateObj);
        
        log.info("[recalculateBundleCostPrice] 重新计算成本价成功，ID={}, 新成本价={}", 
                 bundleProductId, totalCost);
    }

    /**
     * 批量重新计算组合产品成本价（定时任务使用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchRecalculateBundleCostPrice() {
        // 查询所有自动累计模式的组合产品
        List<ProductInfoDO> bundleProducts = productInfoMapper
                .selectListByProductTypeAndBundleType(1, 2);
        
        for (ProductInfoDO product : bundleProducts) {
            try {
                recalculateBundleCostPrice(product.getId());
            } catch (Exception e) {
                log.error("[batchRecalculateBundleCostPrice] 重新计算产品{}成本价失败", 
                          product.getId(), e);
            }
        }
        
        log.info("[batchRecalculateBundleCostPrice] 批量重新计算完成，共{}个", bundleProducts.size());
    }
}

