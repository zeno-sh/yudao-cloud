package cn.iocoder.yudao.module.dm.controller.admin.plan;

import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.dm.service.plan.ProductCostService;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.math.BigDecimal;
import java.util.*;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.dm.controller.admin.plan.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.plan.ProductSelectionPlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.service.plan.ProductSelectionPlanService;

@Tag(name = "管理后台 - 选品计划")
@RestController
@RequestMapping("/dm/product-selection-plan")
@Validated
public class ProductSelectionPlanController {

    @Resource
    private ProductSelectionPlanService productSelectionPlanService;
    @Resource
    private ProductCostService productCostService;
    @Resource
    private ProductInfoService productInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建选品计划")
    @PreAuthorize("@ss.hasPermission('dm:product-selection-plan:create')")
    public CommonResult<Long> createProductSelectionPlan(@Valid @RequestBody ProductSelectionPlanSaveReqVO createReqVO) {
        return success(productSelectionPlanService.createProductSelectionPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新选品计划")
    @PreAuthorize("@ss.hasPermission('dm:product-selection-plan:update')")
    public CommonResult<Boolean> updateProductSelectionPlan(@Valid @RequestBody ProductSelectionPlanSaveReqVO updateReqVO) {
        productSelectionPlanService.updateProductSelectionPlan(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除选品计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:product-selection-plan:delete')")
    public CommonResult<Boolean> deleteProductSelectionPlan(@RequestParam("id") Long id) {
        productSelectionPlanService.deleteProductSelectionPlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得选品计划")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:product-selection-plan:query')")
    public CommonResult<ProductSelectionPlanRespVO> getProductSelectionPlan(@RequestParam("id") Long id) {
        ProductSelectionPlanDO productSelectionPlan = productSelectionPlanService.getProductSelectionPlan(id);
        return success(BeanUtils.toBean(productSelectionPlan, ProductSelectionPlanRespVO.class));
    }

    @Deprecated
    @GetMapping("/pageOld")
    @Operation(summary = "获得选品计划分页")
    @PreAuthorize("@ss.hasPermission('dm:product-selection-plan:query')")
    public CommonResult<PageResult<ProductSelectionPlanRespVO>> getProductSelectionPlanPageOld(@Valid ProductSelectionPlanPageReqVO pageReqVO) {
        PageResult<ProductSelectionPlanDO> pageResult = productSelectionPlanService.getProductSelectionPlanPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProductSelectionPlanRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得选品计划分页")
    @PreAuthorize("@ss.hasPermission('dm:product-selection-plan:query')")
    public CommonResult<PageResult<ProductPlanVO>> getProductSelectionPlanPage(@Valid ProductSelectionPlanPageReqVO pageReqVO) {
        PageResult<ProductSelectionPlanDO> pageResult = productSelectionPlanService.getProductSelectionPlanPage(pageReqVO);
        List<ProductSelectionPlanDO> planList = pageResult.getList();
        List<ProductPlanVO> planVOList = productCostService.createPlanVO(planList);
        PageResult<ProductPlanVO> pageVoResult = new PageResult<>(planVOList, pageResult.getTotal());
        return success(pageVoResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出选品计划 Excel")
    @PreAuthorize("@ss.hasPermission('dm:product-selection-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductSelectionPlanExcel(@Valid ProductSelectionPlanPageReqVO pageReqVO,
                                                HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProductSelectionPlanDO> list = productSelectionPlanService.getProductSelectionPlanPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "选品计划.xls", "数据", ProductSelectionPlanRespVO.class,
                BeanUtils.toBean(list, ProductSelectionPlanRespVO.class));
    }

    // ==================== 一键生成选品计划 ====================
    @PostMapping("/createPlan")
    @Operation(summary = "创建选品计划")
    @PreAuthorize("@ss.hasPermission('dm:product-selection-plan:create')")
    public CommonResult<Long> createPlan(@Valid @RequestBody ProductPlanAddVO productPlanAddVO) {

        String planName = StringUtils.isBlank(productPlanAddVO.getPlanName()) ? "默认选品计划" : productPlanAddVO.getPlanName();
        String productIdStr = productPlanAddVO.getProductId();

        String[] productIds = productIdStr.split(",");
        if (productIds.length > 0) {
            List<ProductInfoDO> productInfoDOS = productInfoService.batchQueryProductInfoList(Arrays.stream(productIds)
                    .map(Long::valueOf)
                    .collect(Collectors.toList()));
            Map<Long, ProductInfoDO> productInfoDOMap = productInfoDOS.stream().collect(Collectors.toMap(ProductInfoDO::getId, Function.identity(), (v1, v2) -> v1));
            for (String productId : productIds) {
                ProductSelectionPlanSaveReqVO createReqVO = new ProductSelectionPlanSaveReqVO();
                createReqVO.setPlanName(planName);
                createReqVO.setProductId(Long.valueOf(productId));
                createReqVO.setForwarderPrice(new BigDecimal(productPlanAddVO.getForwarderPrice()));
                createReqVO.setPlanSkuId(productInfoDOMap.get(Long.valueOf(productId)).getSkuId());
                productSelectionPlanService.createProductSelectionPlan(createReqVO);
            }
            return success((long) productIds.length);
        }

        return error(ErrorCodeConstants.PRODUCT_SELECTION_PLAN_FAIL);
    }


    // ==================== 子表（采购信息） ====================

    @GetMapping("/product-purchase/list-by-product-id")
    @Operation(summary = "获得采购信息列表")
    @Parameter(name = "productId", description = "产品Id")
    @PreAuthorize("@ss.hasPermission('dm:product-selection-plan:query')")
    public CommonResult<List<ProductPurchaseDO>> getProductPurchaseListByProductId(@RequestParam("productId") Long productId) {
        return success(productSelectionPlanService.getProductPurchaseListByProductId(productId));
    }

}