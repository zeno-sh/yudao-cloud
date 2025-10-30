package cn.iocoder.yudao.module.dm.service.product;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.datapermission.core.util.DataPermissionUtils;
import cn.iocoder.yudao.module.dm.dal.dataobject.plan.ProductSelectionPlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.supplier.ProductSupplierDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanItemDO;
import cn.iocoder.yudao.module.dm.dal.mysql.commission.CategoryCommissionMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.product.*;
import cn.iocoder.yudao.module.dm.dal.mysql.productcosts.ProductCostsMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.supplier.ProductSupplierMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.transport.TransportPlanItemMapper;
import cn.iocoder.yudao.module.dm.service.plan.ProductSelectionPlanService;
import cn.iocoder.yudao.module.dm.service.purchase.plan.PurchasePlanItemService;

import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.dm.controller.admin.product.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 产品信息 Service 实现类
 *
 * @author zeno
 */
@Service
@Validated
public class ProductInfoServiceImpl implements ProductInfoService {

    @Resource
    private ProductInfoMapper productInfoMapper;
    @Resource
    private ProductCustomsMapper productCustomsMapper;
    @Resource
    private ProductPriceMapper productPriceMapper;
    @Resource
    private ProductPlatformTrendMapper productPlatformTrendMapper;
    @Resource
    private ProductPurchaseMapper productPurchaseMapper;
    @Resource
    private SupplierPriceOfferMapper supplierPriceOfferMapper;
    @Resource
    private CategoryCommissionMapper categoryCommissionMapper;
    @Resource
    private ConfigApi configApi;
    @Resource
    private ProductSupplierMapper productSupplierMapper;
    @Resource
    private ProductSelectionPlanService productSelectionPlanService;
    @Resource
    private PurchasePlanItemService purchasePlanItemService;
    @Resource
    private TransportPlanItemMapper transportPlanItemMapper;
    @Resource
    private ProductCostsMapper productCostsMapper;
    @Resource
    private ProductBundleRelationMapper bundleRelationMapper;
    @Resource
    private BundleProductServiceHelper bundleProductServiceHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductInfo(ProductInfoSaveReqVO createReqVO) {
        validateSkuExists(createReqVO.getSkuId(), createReqVO.getModelNumber());
        
        // ========== 组合产品校验（新增）==========
        if (createReqVO.getProductType() != null && createReqVO.getProductType() == 1) {
            bundleProductServiceHelper.validateBundleProduct(createReqVO);
        }
        
        // 插入
        ProductInfoDO productInfo = BeanUtils.toBean(createReqVO, ProductInfoDO.class);
        
        // ========== 组合产品成本价计算（新增）==========
        if (productInfo.getProductType() != null && productInfo.getProductType() == 1) {
            // 创建组合关系（临时，用于计算成本价）
            List<ProductBundleRelationDO> tempRelations = 
                bundleProductServiceHelper.createBundleRelations(null, createReqVO.getBundleItems());
            
            // 计算并设置成本价
            BigDecimal calculatedCost = bundleProductServiceHelper.calculateBundleCostPrice(
                productInfo.getBundleType(), 
                productInfo.getCostPrice(), 
                tempRelations
            );
            productInfo.setCostPrice(calculatedCost);
        }
        
        productInfoMapper.insert(productInfo);

        // 插入子表
        createProductCustoms(productInfo.getId(), createReqVO.getProductCustoms());
        createProductPriceList(productInfo.getId(), createReqVO.getProductPrices());
        createProductPlatformTrendList(productInfo.getId(), createReqVO.getProductPlatformTrends());
        createProductPurchaseList(productInfo.getId(), createReqVO.getProductPurchases());
        createSupplierPriceOfferList(productInfo.getId(), createReqVO.getSupplierPriceOffers());
        createProductCostList(productInfo.getId(), createReqVO.getProductCosts());
        
        // ========== 插入组合产品关系（新增）==========
        if (productInfo.getProductType() != null && productInfo.getProductType() == 1) {
            List<ProductBundleRelationDO> relations = 
                bundleProductServiceHelper.createBundleRelations(productInfo.getId(), createReqVO.getBundleItems());
            for (ProductBundleRelationDO relation : relations) {
                bundleRelationMapper.insert(relation);
            }
        }
        
        // 返回
        return productInfo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductInfo(ProductInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateProductInfoExists(updateReqVO.getId());
        
        // ========== 组合产品校验（新增）==========
        if (updateReqVO.getProductType() != null && updateReqVO.getProductType() == 1) {
            bundleProductServiceHelper.validateBundleProduct(updateReqVO);
        }
        
        // 更新
        ProductInfoDO updateObj = BeanUtils.toBean(updateReqVO, ProductInfoDO.class);
        
        // ========== 组合产品成本价计算（新增）==========
        if (updateObj.getProductType() != null && updateObj.getProductType() == 1) {
            // 创建组合关系（临时，用于计算成本价）
            List<ProductBundleRelationDO> tempRelations = 
                bundleProductServiceHelper.createBundleRelations(updateReqVO.getId(), updateReqVO.getBundleItems());
            
            // 计算并设置成本价
            BigDecimal calculatedCost = bundleProductServiceHelper.calculateBundleCostPrice(
                updateObj.getBundleType(), 
                updateObj.getCostPrice(), 
                tempRelations
            );
            updateObj.setCostPrice(calculatedCost);
        }

        replacePictureUrl(updateObj);
        productInfoMapper.updateById(updateObj);

        // 更新子表
        updateProductCustoms(updateReqVO.getId(), updateReqVO.getProductCustoms());
        updateProductPriceList(updateReqVO.getId(), updateReqVO.getProductPrices());
        updateProductPlatformTrendList(updateReqVO.getId(), updateReqVO.getProductPlatformTrends());
        updateProductPurchaseList(updateReqVO.getId(), updateReqVO.getProductPurchases());
        updateSupplierPriceOfferList(updateReqVO.getId(), updateReqVO.getSupplierPriceOffers());
        updateProductCostList(updateReqVO.getId(), updateReqVO.getProductCosts());
        
        // ========== 更新组合产品关系（新增）==========
        if (updateObj.getProductType() != null && updateObj.getProductType() == 1) {
            updateBundleRelations(updateReqVO.getId(), updateReqVO.getBundleItems());
        }
    }

    private void updateProductCostList(Long productId, List<ProductCostsDO> productCosts) {
        productCosts.forEach(o -> o.setProductId(productId));
        List<ProductCostsDO> existList = productCostsMapper.selectList(ProductCostsDO::getProductId, productId);
        productCostsMapper.updateEntityList(productCostsMapper, existList, productCosts, ProductCostsDO::getId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductInfo(Long id) {
        // 校验存在
        validateProductInfoExists(id);
        // 校验关联性
        validateRelevance(id);
        // 删除
        productInfoMapper.deleteById(id);

        // 删除子表
        deleteProductCustomsByProductId(id);
        deleteProductPriceByProductId(id);
        deleteProductPlatformTrendByProductId(id);
        deleteProductPurchaseByProductId(id);
        deleteSupplierPriceOfferByProductId(id);
    }

    private void validateRelevance(Long id) {
        List<ProductSelectionPlanDO> productSelectionPlanDOList = productSelectionPlanService.getProductSelectionPlanByProductId(id);
        if (CollectionUtils.isNotEmpty(productSelectionPlanDOList)) {
            throw exception(PRODUCT_INFO_SELECTION_PLAN_EXISTS);
        }

        List<PurchasePlanItemDO> purchasePlanItemList = purchasePlanItemService.getPurchasePlanItemByProductId(id);
        if (CollectionUtils.isNotEmpty(purchasePlanItemList)) {
            throw exception(PRODUCT_INFO_PURCHASE_PLAN_EXISTS);
        }

        List<TransportPlanItemDO> transportPlanItemList = transportPlanItemMapper.selectList(TransportPlanItemDO::getProductId, id);
        if (CollectionUtils.isNotEmpty(transportPlanItemList)) {
            throw exception(PRODUCT_INFO_TRANSPORT_EXISTS);
        }
    }

    private void validateProductInfoExists(Long id) {
        DataPermissionUtils.executeIgnore(() -> {
            if (productInfoMapper.selectById(id) == null) {
                throw exception(PRODUCT_INFO_NOT_EXISTS);
            }
            return null;
        });
    }

    @Override
    public ProductInfoDO getProductInfo(Long id) {
        return DataPermissionUtils.executeIgnore(() -> {
            ProductInfoDO productInfoDO = productInfoMapper.selectById(id);
            if (productInfoDO == null) {
                return null;
            }
//            productInfoDO.setCostPrice(getProductPurchasePrice(productInfoDO.getId()));
            replacePictureUrl(productInfoDO);
            return productInfoDO;
        });
    }

    @Override
    public ProductInfoDO getProductInfoBySkuId(String skuId) {
        return DataPermissionUtils.executeIgnore(() -> {
            return productInfoMapper.selectOne(ProductInfoDO::getSkuId, skuId);
        });
    }

    @Override
    public PageResult<ProductInfoDO> getProductInfoPage(ProductInfoPageReqVO pageReqVO) {
        PageResult<ProductInfoDO> productInfoDOPageResult = productInfoMapper.selectPage(pageReqVO);
        List<ProductInfoDO> productInfoDOList = productInfoDOPageResult.getList();
//        if (CollectionUtils.isNotEmpty(productInfoDOList)) {
//
//            List<Long> productIds = convertList(productInfoDOList, ProductInfoDO::getId);
//            Map<Long, SupplierPriceOfferDO> priceMap = getDefaultSupplierPriceOfferMap(productIds);
//            for (ProductInfoDO productInfo : productInfoDOList) {
//                replacePictureUrl(productInfo);
//
//                Long productId = productInfo.getId();
//                SupplierPriceOfferDO supplierPriceOfferDO = priceMap.get(productId);
//                productInfo.setCostPrice(supplierPriceOfferDO == null ? BigDecimal.ZERO : supplierPriceOfferDO.getPrice());
//            }
//        }

        productInfoDOPageResult.setList(productInfoDOList);
        return productInfoDOPageResult;
    }

    private BigDecimal getProductPurchasePrice(Long productId) {
        List<SupplierPriceOfferDO> offerDOList = getSupplierPriceOfferListByProductId(productId);
        if (CollectionUtils.isNotEmpty(offerDOList)) {
            return offerDOList.stream()
                    .filter(product -> "Y".equals(product.getFirstChoice()))
                    .map(SupplierPriceOfferDO::getPrice)
                    .findFirst()
                    .orElse(offerDOList.get(0).getPrice());
        }
        return BigDecimal.ZERO;
    }

    private Map<Long, SupplierPriceOfferDO> getDefaultSupplierPriceOfferMap(List<Long> productIds) {
        Map<Long, List<SupplierPriceOfferDO>> priceMap = batchProductPriceOfferListByProductIds(productIds);
        if (priceMap.isEmpty()) {
            return Collections.emptyMap();
        }

        return priceMap.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .filter(offer -> "Y".equals(offer.getFirstChoice()))
                                .findFirst()
                                .orElse(entry.getValue().get(0))
                ));
    }

    private Map<Long, List<SupplierPriceOfferDO>> batchProductPriceOfferListByProductIds(List<Long> productIds) {
        List<SupplierPriceOfferDO> supplierPriceOfferDOS = supplierPriceOfferMapper.selectList(SupplierPriceOfferDO::getProductId, productIds);
        if (CollectionUtils.isEmpty(supplierPriceOfferDOS)) {
            return Collections.emptyMap();
        }
        return supplierPriceOfferDOS.stream().collect(Collectors.groupingBy(SupplierPriceOfferDO::getProductId));
    }

    @Override
    public List<ProductInfoDO> batchQueryProductInfoList(List<Long> productIds) {
        return DataPermissionUtils.executeIgnore(() -> {
            List<ProductInfoDO> productInfoDOS = productInfoMapper.selectList(ProductInfoDO::getId, productIds);
            if (CollectionUtils.isEmpty(productInfoDOS)) {
                return productInfoDOS;
            }
            productInfoDOS.forEach(productInfoDO -> replacePictureUrl(productInfoDO));
            return productInfoDOS;
        });
    }

    private void replacePictureUrl(ProductInfoDO productInfo) {
        String pictureUrl = productInfo.getPictureUrl();
        String filePath = configApi.getConfigValueByKey("image_url_repleace").getCheckedData();
        if (StringUtils.isNotBlank(filePath) && pictureUrl.contains("/profile/upload/")) {
            String newUrl = pictureUrl.replace("/profile/upload/", filePath);
            productInfo.setPictureUrl(newUrl);
        }
    }

    @Override
    public List<ProductInfoDO> queryByKeyword(String keyword) {
        List<ProductInfoDO> productInfoDOS = productInfoMapper.selectList(new LambdaQueryWrapper<ProductInfoDO>()
                .like(ProductInfoDO::getSkuId, keyword)
                .or()
                .like(ProductInfoDO::getSkuName, keyword)
        );

        if (CollectionUtils.isEmpty(productInfoDOS)) {
            return productInfoDOS;
        }
        productInfoDOS.forEach(productInfoDO -> replacePictureUrl(productInfoDO));
        return productInfoDOS;
    }

    @Override
    public List<ProductInfoDO> queryProductInfoListBySpuId(String spuId) {
        return productInfoMapper.selectList(new LambdaQueryWrapperX<ProductInfoDO>()
                .likeIfPresent(ProductInfoDO::getModelNumber, spuId));
    }

    // ==================== 子表（海关信息） ====================

    @Override
    public ProductCustomsDO getProductCustomsByProductId(Long productId) {
        return productCustomsMapper.selectByProductId(productId);
    }

    private void createProductCustoms(Long productId, ProductCustomsDO productCustoms) {
        if (productCustoms == null) {
            return;
        }
        productCustoms.setProductId(productId);
        productCustomsMapper.insert(productCustoms);
    }

    private void updateProductCustoms(Long productId, ProductCustomsDO productCustoms) {
        if (productCustoms == null) {
            return;
        }
        productCustoms.setProductId(productId);
        productCustoms.setUpdater(null).setUpdateTime(null); // 解决更新情况下：updateTime 不更新
        productCustomsMapper.insertOrUpdate(productCustoms);
    }

    private void deleteProductCustomsByProductId(Long productId) {
        productCustomsMapper.deleteByProductId(productId);
    }

    // ==================== 子表（产品价格策略） ====================

    @Override
    public List<ProductPriceDO> getProductPriceListByProductId(Long productId) {
        return productPriceMapper.selectListByProductId(productId);
    }

    @Override
    public ProductPriceDO getProductPrice(Long id) {
        return productPriceMapper.selectById(id);
    }

    private void createProductPriceList(Long productId, List<ProductPriceDO> list) {
        list.forEach(o -> o.setProductId(productId));
        productPriceMapper.insertBatch(list);
    }

    private void updateProductPriceList(Long productId, List<ProductPriceDO> list) {
        list.forEach(o -> o.setProductId(productId));
        List<ProductPriceDO> existList = productPriceMapper.selectListByProductId(productId);
        productPriceMapper.updateEntityList(productPriceMapper, existList, list, ProductPriceDO::getId);
    }

    private void deleteProductPriceByProductId(Long productId) {
        productPriceMapper.deleteByProductId(productId);
    }

    // ==================== 子表（产品销量趋势） ====================

    @Override
    public List<ProductPlatformTrendDO> getProductPlatformTrendListByProductId(Long productId) {
        return productPlatformTrendMapper.selectListByProductId(productId);
    }

    private void createProductPlatformTrendList(Long productId, List<ProductPlatformTrendDO> list) {
        list.forEach(o -> o.setProductId(productId));
        productPlatformTrendMapper.insertBatch(list);
    }

    private void updateProductPlatformTrendList(Long productId, List<ProductPlatformTrendDO> list) {
        list.forEach(o -> o.setProductId(productId));
        List<ProductPlatformTrendDO> existList = productPlatformTrendMapper.selectList(ProductPlatformTrendDO::getProductId, productId);
        productPlatformTrendMapper.updateEntityList(productPlatformTrendMapper, existList, list, ProductPlatformTrendDO::getId);
    }

    private void deleteProductPlatformTrendByProductId(Long productId) {
        productPlatformTrendMapper.deleteByProductId(productId);
    }

    // ==================== 子表（采购信息） ====================

    @Override
    public List<ProductPurchaseDO> getProductPurchaseListByProductId(Long productId) {
        return productPurchaseMapper.selectListByProductId(productId);
    }

    @Override
    public Map<Long, ProductPurchaseDO> batchProductPurchaseListByProductIds(Long[] productIds) {
        // 查询数据库获取所有匹配的 ProductPurchaseDO 列表
        List<ProductPurchaseDO> productPurchaseList = productPurchaseMapper.batchQueryByProductIds(Arrays.asList(productIds));

        // 如果查询结果为空，则返回空的 Map
        if (CollectionUtils.isEmpty(productPurchaseList)) {
            return Collections.emptyMap();
        }

        // 将列表按照 productId 进行分组，并在每个组中过滤 getFirstChoice 为 'Y' 的元素，如果没有则取第一个元素
        return productPurchaseList.stream()
                .collect(Collectors.groupingBy(
                        ProductPurchaseDO::getProductId, // 按 productId 分组
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .filter(productPurchaseDO -> "Y".equals(productPurchaseDO.getFirstChoice()))
                                        .findFirst()
                                        .orElse(list.get(0)) // 如果没有匹配到 'Y'，则取列表的第一个元素
                        )
                ));
    }

    private void createProductPurchaseList(Long productId, List<ProductPurchaseDO> list) {
        list.forEach(o -> o.setProductId(productId));
        productPurchaseMapper.insertBatch(list);
    }

    private void updateProductPurchaseList(Long productId, List<ProductPurchaseDO> list) {
        list.forEach(o -> o.setProductId(productId));
        List<ProductPurchaseDO> existList = productPurchaseMapper.selectListByProductId(productId);
        productPurchaseMapper.updateEntityList(productPurchaseMapper, existList, list, ProductPurchaseDO::getId);
    }

    private void deleteProductPurchaseByProductId(Long productId) {
        productPurchaseMapper.deleteByProductId(productId);
    }

    // ==================== 子表（供应商报价） ====================

    @Override
    public List<SupplierPriceOfferDO> getSupplierPriceOfferListByProductId(Long productId) {
        return supplierPriceOfferMapper.selectListByProductId(productId);
    }

    @Override
    public ProductSupplierPriceOfferRespVO getSupplierPriceOffer(Long id) {
        SupplierPriceOfferDO supplierPriceOfferDO = supplierPriceOfferMapper.selectById(id);
        if (supplierPriceOfferDO == null) {
            return null;
        }
        ProductSupplierDO productSupplierDO = productSupplierMapper.selectOne(ProductSupplierDO::getSupplierCode, supplierPriceOfferDO.getSupplierCode());
        ProductSupplierPriceOfferRespVO vo = new ProductSupplierPriceOfferRespVO();
        vo.setPriceId(supplierPriceOfferDO.getId());
        vo.setSupplierName(productSupplierDO.getSupplierName());
        vo.setPrice(supplierPriceOfferDO.getPrice().toString());
        return vo;
    }

    @Override
    public Map<Long, ProductSimpleInfoVO> batchQueryProductSimpleInfo(List<Long> productIds) {
        List<ProductInfoDO> productInfoDOList = batchQueryProductInfoList(productIds);

        return productInfoDOList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        ProductInfoDO::getId,
                        productInfoDO -> {
                            ProductSimpleInfoVO productSimpleInfoVO = new ProductSimpleInfoVO();
                            productSimpleInfoVO.setImage(productInfoDO.getPictureUrl());
                            productSimpleInfoVO.setSkuId(productInfoDO.getSkuId());
                            productSimpleInfoVO.setSkuName(productInfoDO.getSkuName());
                            productSimpleInfoVO.setProductId(productInfoDO.getId());
                            return productSimpleInfoVO;
                        },
                        (existing, replacement) -> existing // 处理键冲突的策略
                ));
    }

    @Override
    public Map<String, Long> batchQueryProductIdsBySkuIds(List<String> skuIds) {
        return DataPermissionUtils.executeIgnore(() -> {
            List<ProductInfoDO> productInfoDOS = productInfoMapper.selectList(ProductInfoDO::getSkuId, skuIds);
            if (CollectionUtils.isEmpty(productInfoDOS)) {
                return Maps.newHashMap();
            }
            return convertMap(productInfoDOS, ProductInfoDO::getSkuId, ProductInfoDO::getId);
        });
    }

    @Override
    public Map<Long, SupplierPriceOfferDO> getSupplierPriceOfferMapByProductIds(Collection<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return Collections.emptyMap();
        }
        
        // 批量查询所有产品的供应商报价
        List<SupplierPriceOfferDO> allPriceOffers = supplierPriceOfferMapper.selectList(SupplierPriceOfferDO::getProductId, productIds);
        if (CollectionUtils.isEmpty(allPriceOffers)) {
            return Collections.emptyMap();
        }

        // 按产品ID分组
        Map<Long, List<SupplierPriceOfferDO>> productPriceMap = allPriceOffers.stream()
                .collect(Collectors.groupingBy(SupplierPriceOfferDO::getProductId));

        // 获取每个产品的首选报价或第一个报价
        return productPriceMap.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                            .filter(offer -> "Y".equals(offer.getFirstChoice()))
                            .findFirst()
                            .orElse(entry.getValue().get(0))
                ));
    }

    private void createSupplierPriceOfferList(Long productId, List<SupplierPriceOfferDO> list) {
        list.forEach(o -> o.setProductId(productId));
        supplierPriceOfferMapper.insertBatch(list);
    }

    private void updateSupplierPriceOfferList(Long productId, List<SupplierPriceOfferDO> list) {
        list.forEach(o -> o.setProductId(productId));
        List<SupplierPriceOfferDO> existList = supplierPriceOfferMapper.selectListByProductId(productId);
        supplierPriceOfferMapper.updateEntityList(supplierPriceOfferMapper, existList, list, SupplierPriceOfferDO::getId);
    }

    private void deleteSupplierPriceOfferByProductId(Long productId) {
        supplierPriceOfferMapper.deleteByProductId(productId);
    }

    private void createProductCostList(Long productId, List<ProductCostsDO> productCosts) {
        if (CollectionUtils.isNotEmpty(productCosts)) {
            productCosts.forEach(o -> o.setProductId(productId));
            productCostsMapper.insertBatch(productCosts);
        }
    }

    private void validateSkuExists(String skuId, String modelNumber) {
        // 忽略数据权限，避免产品权限规则影响SKU唯一性校验
        DataPermissionUtils.executeIgnore(() -> {
            LambdaQueryWrapper<ProductInfoDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductInfoDO::getSkuId, skuId);
            if (StringUtils.isNotBlank(modelNumber)) {
                wrapper.eq(ProductInfoDO::getModelNumber, modelNumber);
            }
            ProductInfoDO productInfoDO = productInfoMapper.selectOne(wrapper);
            if (null != productInfoDO) {
                throw exception(PRODUCT_INFO_SKU_EXISTS);
            }
        });
    }
    
    // ==================== 组合产品相关方法（新增）====================
    
    @Override
    public List<ProductBundleRelationDO> getBundleRelations(Long bundleProductId) {
        return bundleRelationMapper.selectListByBundleProductId(bundleProductId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateBundleCostPrice(Long bundleProductId) {
        bundleProductServiceHelper.recalculateBundleCostPrice(bundleProductId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRecalculateBundleCostPrice() {
        bundleProductServiceHelper.batchRecalculateBundleCostPrice();
    }
    
    /**
     * 更新组合产品关系
     */
    private void updateBundleRelations(Long bundleProductId, List<ProductBundleItemReqVO> itemReqVOs) {
        // 1. 删除旧关系
        bundleRelationMapper.deleteByBundleProductId(bundleProductId);
        
        // 2. 插入新关系
        if (CollectionUtils.isNotEmpty(itemReqVOs)) {
            List<ProductBundleRelationDO> relations = 
                bundleProductServiceHelper.createBundleRelations(bundleProductId, itemReqVOs);
            for (ProductBundleRelationDO relation : relations) {
                bundleRelationMapper.insert(relation);
            }
        }
    }

}