package cn.iocoder.yudao.module.dm.service.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.plan.vo.ProductSelectionPlanPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.plan.vo.ProductSelectionPlanSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.plan.ProductSelectionPlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPriceDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.SupplierPriceOfferDO;
import cn.iocoder.yudao.module.dm.dal.mysql.plan.ProductSelectionPlanMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductInfoMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductPriceMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.product.ProductPurchaseMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.product.SupplierPriceOfferMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 选品计划 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class ProductSelectionPlanServiceImpl implements ProductSelectionPlanService {

    @Resource
    private ProductSelectionPlanMapper productSelectionPlanMapper;
    @Resource
    private ProductPurchaseMapper productPurchaseMapper;
    @Resource
    private SupplierPriceOfferMapper supplierPriceOfferMapper;
    @Resource
    private ProductPriceMapper productPriceMapper;
    @Resource
    private ProductInfoMapper productInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProductSelectionPlan(ProductSelectionPlanSaveReqVO createReqVO) {
        // 插入
        ProductSelectionPlanDO productSelectionPlan = BeanUtils.toBean(createReqVO, ProductSelectionPlanDO.class);

        // 测算需要的关键信息
        // 1.采购信息，单品的规格、毛重
        // 2.供应商报价，采购成本
        // 3.价格策略，前端的售价
        // 4.类目佣金
        Long productId = createReqVO.getProductId();

        // 1.采购信息 4.类目佣金
        validatePurchaseList(productId);
        validateCommission(productId);

        // 2.供应商报价
        Long supplierPriceOfferId = getSupplierPriceOfferId(productId);
        productSelectionPlan.setSupplierPriceOfferId(supplierPriceOfferId);

        // 3.价格策略
        Long priceStrategyId = getPriceStrategyId(productId);
        productSelectionPlan.setPriceId(priceStrategyId);

        productSelectionPlanMapper.insert(productSelectionPlan);
        // 返回
        return productSelectionPlan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductSelectionPlan(ProductSelectionPlanSaveReqVO updateReqVO) {
        // 校验存在
        validateProductSelectionPlanExists(updateReqVO.getId());
        // 更新
        ProductSelectionPlanDO updateObj = BeanUtils.toBean(updateReqVO, ProductSelectionPlanDO.class);
        productSelectionPlanMapper.updateById(updateObj);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProductSelectionPlan(Long id) {
        // 校验存在
        validateProductSelectionPlanExists(id);
        // 删除
        productSelectionPlanMapper.deleteById(id);
    }

    private void validateProductSelectionPlanExists(Long id) {
        if (productSelectionPlanMapper.selectById(id) == null) {
            throw exception(PRODUCT_SELECTION_PLAN_NOT_EXISTS);
        }
    }

    @Override
    public ProductSelectionPlanDO getProductSelectionPlan(Long id) {
        return productSelectionPlanMapper.selectById(id);
    }

    @Override
    public PageResult<ProductSelectionPlanDO> getProductSelectionPlanPage(ProductSelectionPlanPageReqVO pageReqVO) {
        return productSelectionPlanMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProductSelectionPlanDO> getProductSelectionPlanByProductId(Long productId) {
        return productSelectionPlanMapper.selectList(ProductSelectionPlanDO::getProductId, productId);
    }

    // ==================== 子表（采购信息） ====================

    @Override
    public List<ProductPurchaseDO> getProductPurchaseListByProductId(Long productId) {
        return productPurchaseMapper.selectListByProductId(productId);
    }

    private void validatePurchaseList(Long productId) {
        List<ProductPurchaseDO> productPurchaseDOList = getProductPurchaseListByProductId(productId);
        if (CollectionUtils.isEmpty(productPurchaseDOList)) {
            throw exception(PRODUCT_SELECTION_PURCHASE_NOT_EXIST);
        }
    }

    private Long getSupplierPriceOfferId(Long productId) {
        List<SupplierPriceOfferDO> priceOfferDOList = supplierPriceOfferMapper.selectListByProductId(productId);
        if (CollectionUtils.isEmpty(priceOfferDOList)) {
            throw exception(PRODUCT_SELECTION_SUPPLIER_PRICE_OFFER_NOT_EXIST);
        }
        return priceOfferDOList.stream()
                .filter(product -> "Y".equals(product.getFirstChoice()))
                .map(SupplierPriceOfferDO::getId)
                .findFirst()
                .orElse(priceOfferDOList.get(0).getId());
    }

    private Long getPriceStrategyId(Long productId) {
        List<ProductPriceDO> productPriceDOList = productPriceMapper.selectList(ProductPriceDO::getProductId, productId);
        if (CollectionUtils.isEmpty(productPriceDOList)) {
            throw exception(PRODUCT_SELECTION_PRICE_STRATEGY_NOT_EXIST);
        }
        return productPriceDOList.stream()
                .filter(product -> "Y".equals(product.getFirstChoice()))
                .map(ProductPriceDO::getId)
                .findFirst()
                .orElse(productPriceDOList.get(0).getId());
    }

    private void validateCommission(Long productId) {
        ProductInfoDO productInfoDO = productInfoMapper.selectOne(ProductInfoDO::getId, productId);
        Long categoryCommissionId = productInfoDO.getCategoryCommissionId();
        if (null == categoryCommissionId) {
            throw exception(PRODUCT_SELECTION_PLAN_CATEGORY_COMMISSION_NOT_EXIST);
        }
    }
}