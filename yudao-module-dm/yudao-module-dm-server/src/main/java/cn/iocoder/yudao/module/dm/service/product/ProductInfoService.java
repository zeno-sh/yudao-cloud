package cn.iocoder.yudao.module.dm.service.product;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.dm.controller.admin.product.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductCustomsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPriceDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPlatformTrendDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.SupplierPriceOfferDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 产品信息 Service 接口
 *
 * @author zeno
 */
public interface ProductInfoService {

    /**
     * 创建产品信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductInfo(@Valid ProductInfoSaveReqVO createReqVO);

    /**
     * 更新产品信息
     *
     * @param updateReqVO 更新信息
     */
    void updateProductInfo(@Valid ProductInfoSaveReqVO updateReqVO);

    /**
     * 删除产品信息
     *
     * @param id 编号
     */
    void deleteProductInfo(Long id);

    /**
     * 获得产品信息
     *
     * @param id 编号
     * @return 产品信息
     */
    ProductInfoDO getProductInfo(Long id);

    /**
     * 获得产品信息
     *
     * @param skuId skuId
     * @return 产品信息
     */
    ProductInfoDO getProductInfoBySkuId(String skuId);

    /**
     * 获得产品信息分页
     *
     * @param pageReqVO 分页查询
     * @return 产品信息分页
     */
    PageResult<ProductInfoDO> getProductInfoPage(ProductInfoPageReqVO pageReqVO);

    /**
     * 批量查询产品信息列表
     *
     * @param productIds
     * @return
     */
    List<ProductInfoDO> batchQueryProductInfoList(List<Long> productIds);

    /**
     * 根据关键词查询
     *
     * @param keyword
     * @return
     */
    List<ProductInfoDO> queryByKeyword(String keyword);

    /**
     * 根据spuId查询
     * @param spuId
     * @return
     */
    List<ProductInfoDO> queryProductInfoListBySpuId(String spuId);

    // ==================== 子表（海关信息） ====================

    /**
     * 获得海关信息
     *
     * @param productId 产品Id
     * @return 海关信息
     */
    ProductCustomsDO getProductCustomsByProductId(Long productId);

    // ==================== 子表（产品价格策略） ====================

    /**
     * 获得产品价格策略列表
     *
     * @param productId 产品Id
     * @return 产品价格策略列表
     */
    List<ProductPriceDO> getProductPriceListByProductId(Long productId);

    /**
     * 获得产品价格策略
     *
     * @param id 编号
     * @return 产品价格策略
     */
    ProductPriceDO getProductPrice(Long id);

    // ==================== 子表（产品销量趋势） ====================

    /**
     * 获得产品销量趋势列表
     *
     * @param productId 产品Id
     * @return 产品销量趋势列表
     */
    List<ProductPlatformTrendDO> getProductPlatformTrendListByProductId(Long productId);

    // ==================== 子表（采购信息） ====================

    /**
     * 获得采购信息列表
     *
     * @param productId 产品Id
     * @return 采购信息列表
     */
    List<ProductPurchaseDO> getProductPurchaseListByProductId(Long productId);

    /**
     * 批量查询采购信息列表
     *
     * @param productIds
     * @return
     */
    Map<Long, ProductPurchaseDO> batchProductPurchaseListByProductIds(Long[] productIds);

    // ==================== 子表（供应商报价） ====================

    /**
     * 获得供应商报价列表
     *
     * @param productId 产品Id
     * @return 供应商报价列表
     */
    List<SupplierPriceOfferDO> getSupplierPriceOfferListByProductId(Long productId);

    /**
     * 获得供应商报价
     *
     * @param id 编号
     * @return 供应商报价
     */
    ProductSupplierPriceOfferRespVO getSupplierPriceOffer(Long id);

    /**
     * 批量查询产品简单信息
     *
     * @param productIds
     * @return
     */
    Map<Long, ProductSimpleInfoVO> batchQueryProductSimpleInfo(List<Long> productIds);

    /**
     * 根据skuId批量查询产品id
     *
     * @param skuIds
     * @return
     */
    Map<String, Long> batchQueryProductIdsBySkuIds(List<String> skuIds);

    /**
     * 批量获取产品的首选供应商报价
     * 
     * @param productIds 产品ID列表
     * @return 产品ID到供应商报价的映射，key是productId，value是首选供应商报价（如无首选则为第一个报价）
     */
    Map<Long, SupplierPriceOfferDO> getSupplierPriceOfferMapByProductIds(Collection<Long> productIds);
}