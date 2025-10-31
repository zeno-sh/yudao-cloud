package cn.iocoder.yudao.module.dm.controller.admin.product;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.productcosts.ProductCostsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 产品信息")
@RestController
@RequestMapping("/dm/product-info")
@Validated
@Slf4j
public class ProductInfoController {

    @Resource
    private ProductInfoService productInfoService;
    @Resource
    private ProductCostsService productCostsService;

    @PostMapping("/create")
    @Operation(summary = "创建产品信息")
    @PreAuthorize("@ss.hasPermission('dm:product-info:create')")
    public CommonResult<Long> createProductInfo(@Valid @RequestBody ProductInfoSaveReqVO createReqVO) {
        return success(productInfoService.createProductInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品信息")
    @PreAuthorize("@ss.hasPermission('dm:product-info:update')")
    public CommonResult<Boolean> updateProductInfo(@Valid @RequestBody ProductInfoSaveReqVO updateReqVO) {
        productInfoService.updateProductInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:product-info:delete')")
    public CommonResult<Boolean> deleteProductInfo(@RequestParam("id") Long id) {
        productInfoService.deleteProductInfo(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<ProductInfoRespVO> getProductInfo(@RequestParam("id") Long id) {
        ProductInfoDO productInfo = productInfoService.getProductInfo(id);
        return success(BeanUtils.toBean(productInfo, ProductInfoRespVO.class));
    }

//    @GetMapping("/getBySkuId")
//    @Operation(summary = "获得产品信息")
//    @Parameter(name = "skuId", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
//    public CommonResult<List<ProductInfoRespVO>> getProductInfoBySkuId(@RequestParam("skuId") String skuId) {
//        List<ProductInfoDO> productInfos = productInfoService.getProductInfoBySkuId(skuId);
//        return success(BeanUtils.toBean(productInfos, ProductInfoRespVO.class));
//    }


    @GetMapping("/getByKeyword")
    @Operation(summary = "获得产品信息")
    @Parameter(name = "keyword", description = "关键词", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<List<ProductInfoRespVO>> getProductInfoByKeyword(@RequestParam("keyword") String keyword) {
        List<ProductInfoDO> productInfos = productInfoService.queryByKeyword(keyword);
        return success(BeanUtils.toBean(productInfos, ProductInfoRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品信息分页")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<PageResult<ProductInfoRespVO>> getProductInfoPage(@Valid ProductInfoPageReqVO pageReqVO) {
        PageResult<ProductInfoDO> pageResult = productInfoService.getProductInfoPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProductInfoRespVO.class, vo -> {
            ProductPurchaseDO productPurchaseDO = getProductPurchaseDO(vo.getId());
            if (null != productPurchaseDO) {
                vo.setPurchaseInfo(String.format("%s * %s * %s", productPurchaseDO.getLength(), productPurchaseDO.getWidth(), productPurchaseDO.getHeight()));
                vo.setGrossWeight(productPurchaseDO.getGrossWeight());
                vo.setBoxInfo(String.format("%s * %s * %s", productPurchaseDO.getBoxLength(), productPurchaseDO.getBoxWidth(), productPurchaseDO.getBoxHeight()));
                vo.setPcs(productPurchaseDO.getQuantityPerBox());
            }
        }));
    }

    private ProductPurchaseDO getProductPurchaseDO(Long productId) {
        List<ProductPurchaseDO> productPurchaseDOList = productInfoService.getProductPurchaseListByProductId(productId);
        if (CollectionUtils.isEmpty(productPurchaseDOList)) {
            return null;
        }

        return productPurchaseDOList.stream()
                .filter(product -> "Y".equals(product.getFirstChoice()))
                .findFirst()
                .orElse(productPurchaseDOList.get(0));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品信息 Excel")
    @PreAuthorize("@ss.hasPermission('dm:product-info:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductInfoExcel(@Valid ProductInfoPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProductInfoDO> list = productInfoService.getProductInfoPage(pageReqVO).getList();

        List<Long> productIds = convertList(list, ProductInfoDO::getId);
        Map<Long, ProductPurchaseDO> purchaseDOMap = productInfoService.batchProductPurchaseListByProductIds(productIds.toArray(new Long[0]));
        // 导出 Excel
        ExcelUtils.write(response, "产品信息.xls", "数据", ProductInfoRespVO.class,
                BeanUtils.toBean(list, ProductInfoRespVO.class, vo -> {
                    ProductPurchaseDO productPurchaseDO = purchaseDOMap.get(vo.getId());
                    if (null != productPurchaseDO) {
                        vo.setPurchaseInfo(String.format("%s * %s * %s", productPurchaseDO.getLength(), productPurchaseDO.getWidth(), productPurchaseDO.getHeight()));
                        vo.setGrossWeight(productPurchaseDO.getGrossWeight());
                        vo.setBoxInfo(String.format("%s * %s * %s", productPurchaseDO.getBoxLength(), productPurchaseDO.getBoxWidth(), productPurchaseDO.getBoxHeight()));
                        vo.setPcs(productPurchaseDO.getQuantityPerBox());
                        vo.setVolume(productPurchaseDO.getLength().multiply(productPurchaseDO.getWidth()).multiply(productPurchaseDO.getHeight())
                                .divide(new BigDecimal("1000000"), 3, RoundingMode.UP));
                    }
                }));
    }

    // ==================== 子表（海关信息） ====================

    @GetMapping("/product-customs/get-by-product-id")
    @Operation(summary = "获得海关信息")
    @Parameter(name = "productId", description = "产品Id")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<ProductCustomsDO> getProductCustomsByProductId(@RequestParam("productId") Long productId) {
        return success(productInfoService.getProductCustomsByProductId(productId));
    }

    // ==================== 子表（产品价格策略） ====================

    @GetMapping("/product-price/list-by-product-id")
    @Operation(summary = "获得产品价格策略列表")
    @Parameter(name = "productId", description = "产品Id")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<List<ProductPriceDO>> getProductPriceListByProductId(@RequestParam("productId") Long productId) {
        return success(productInfoService.getProductPriceListByProductId(productId));
    }

    @GetMapping("/product-price/get")
    @Operation(summary = "获得产品价格策略列表")
    @Parameter(name = "productId", description = "产品Id")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<ProductPriceDO> getProductPrice(@RequestParam("id") Long id) {
        return success(productInfoService.getProductPrice(id));
    }

    // ==================== 子表（产品销量趋势） ====================

    @GetMapping("/product-platform-trend/list-by-product-id")
    @Operation(summary = "获得产品销量趋势列表")
    @Parameter(name = "productId", description = "产品Id")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<List<ProductPlatformTrendDO>> getProductPlatformTrendListByProductId(@RequestParam("productId") Long productId) {
        return success(productInfoService.getProductPlatformTrendListByProductId(productId));
    }

    // ==================== 子表（采购信息） ====================

    @GetMapping("/product-purchase/list-by-product-id")
    @Operation(summary = "获得采购信息列表")
    @Parameter(name = "productId", description = "产品Id")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<List<ProductPurchaseDO>> getProductPurchaseListByProductId(@RequestParam("productId") Long productId) {
        return success(productInfoService.getProductPurchaseListByProductId(productId));
    }

    @GetMapping("/product-purchase/list-by-product-ids")
    @Operation(summary = "获得采购信息列表")
    @Parameter(name = "productId", description = "产品Id")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<Map<Long, ProductPurchaseDO>> getProductPurchaseListByProductIds(@RequestParam("productIds") Long[] productIds) {
        return success(productInfoService.batchProductPurchaseListByProductIds(productIds));
    }

    // ==================== 子表（供应商报价） ====================

    @GetMapping("/supplier-price-offer/list-by-product-id")
    @Operation(summary = "获得供应商报价列表")
    @Parameter(name = "productId", description = "产品Id")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<List<SupplierPriceOfferDO>> getSupplierPriceOfferListByProductId(@RequestParam("productId") Long productId) {
        return success(productInfoService.getSupplierPriceOfferListByProductId(productId));
    }

    @GetMapping("/supplier-price-offer/get")
    @Operation(summary = "获得供应商报价列表")
    @Parameter(name = "productId", description = "产品Id")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<ProductSupplierPriceOfferRespVO> getSupplierPriceOffer(@RequestParam("priceId") Long id) {
        return success(productInfoService.getSupplierPriceOffer(id));
    }


    @GetMapping("/product-cost/get")
    @Operation(summary = "获得供应商报价列表")
    @Parameter(name = "productId", description = "产品Id")
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<List<ProductCostsDO>> getProductCost(@RequestParam("productId") Long productId) {
        return success(productCostsService.getProductCostsListByProductId(productId));
    }


//    private void accept(ProductInfoRespVO vo) {
//        String pictureUrl = vo.getPictureUrl();
//        if (StringUtils.isNotBlank(pictureUrl)) {
//            Long configId = extractConfigId(pictureUrl);
//            if (configId != null) {
//                String path = StrUtil.subAfter(pictureUrl, "/get/", false);
//                path = URLUtil.decode(path);
//                byte[] content = null;
//                try {
//                    content = fileService.getFileContent(configId, path);
//                } catch (Exception e) {
//                   log.error("[accept][extractConfigId({}) path({}) 获取文件内容异常]", configId, path, e);
//                   return;
//                }
//                if (content != null) {
//                    vo.setImageBytes(content);
//                }
//            }
//        }
//    }

    private Long extractConfigId(String url) {
        // 定位 "/file/" 和 "/get/" 的位置
        String fileKey = "/file/";
        String getKey = "/get/";
        int fileIndex = url.indexOf(fileKey);
        int getIndex = url.indexOf(getKey);

        // 检查是否找到了 "/file/" 和 "/get/"
        if (fileIndex != -1 && getIndex != -1 && fileIndex < getIndex) {
            // 截取 "/file/" 和 "/get/" 之间的部分，即 configId
            return Long.valueOf(url.substring(fileIndex + fileKey.length(), getIndex));
        } else {
            return null; // 如果 URL 格式不对，返回 null
        }
    }
    
    // ==================== 组合产品相关 API（新增）====================
    
    @GetMapping("/bundle-relations")
    @Operation(summary = "获取组合产品明细列表")
    @Parameter(name = "bundleProductId", description = "组合产品ID", required = true)
    @PreAuthorize("@ss.hasPermission('dm:product-info:query')")
    public CommonResult<List<ProductBundleItemRespVO>> getBundleRelations(@RequestParam("bundleProductId") Long bundleProductId) {
        // 1. 获取关联关系
        List<cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductBundleRelationDO> relations = 
            productInfoService.getBundleRelations(bundleProductId);
        
        // 2. 转换为 VO（需要 JOIN 产品信息）
        List<ProductBundleItemRespVO> result = new ArrayList<>();
        for (cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductBundleRelationDO relation : relations) {
            // 实时获取子产品信息
            ProductInfoDO subProduct = productInfoService.getProductInfo(relation.getSubProductId());
            if (subProduct == null) {
                continue;
            }
            
            ProductBundleItemRespVO vo = new ProductBundleItemRespVO();
            vo.setId(relation.getId());
            vo.setSubProductId(relation.getSubProductId());
            vo.setQuantity(relation.getQuantity());
            vo.setSortOrder(relation.getSortOrder());
            vo.setRemark(relation.getRemark());
            
            // 填充实时子产品信息
            vo.setSubSkuId(subProduct.getSkuId());
            vo.setSubSkuName(subProduct.getSkuName());
            vo.setUnit(subProduct.getUnit());
            vo.setUnitCostPrice(subProduct.getCostPrice());
            
            // 计算总成本价
            if (subProduct.getCostPrice() != null) {
                vo.setTotalCostPrice(subProduct.getCostPrice().multiply(new java.math.BigDecimal(relation.getQuantity())));
            }
            
            result.add(vo);
        }
        
        return success(result);
    }
    
    @PostMapping("/recalculate-bundle-cost")
    @Operation(summary = "重新计算组合产品成本价（仅自动累计模式）")
    @Parameter(name = "bundleProductId", description = "组合产品ID", required = true)
    @PreAuthorize("@ss.hasPermission('dm:product-info:update')")
    public CommonResult<Boolean> recalculateBundleCostPrice(@RequestParam("bundleProductId") Long bundleProductId) {
        productInfoService.recalculateBundleCostPrice(bundleProductId);
        return success(true);
    }
    
    @PostMapping("/batch-recalculate-bundle-cost")
    @Operation(summary = "批量重新计算所有自动累计模式的组合产品成本价")
    @PreAuthorize("@ss.hasPermission('dm:product-info:update')")
    public CommonResult<Boolean> batchRecalculateBundleCostPrice() {
        productInfoService.batchRecalculateBundleCostPrice();
        return success(true);
    }
}