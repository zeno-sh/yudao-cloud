package cn.iocoder.yudao.module.dm.controller.admin.supplier;

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
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.dm.controller.admin.supplier.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.supplier.ProductSupplierDO;
import cn.iocoder.yudao.module.dm.service.supplier.ProductSupplierService;

@Tag(name = "管理后台 - 供应商信息")
@RestController
@RequestMapping("/dm/product-supplier")
@Validated
public class ProductSupplierController {

    @Resource
    private ProductSupplierService productSupplierService;

    @PostMapping("/create")
    @Operation(summary = "创建供应商信息")
    @PreAuthorize("@ss.hasPermission('dm:product-supplier:create')")
    public CommonResult<Long> createProductSupplier(@Valid @RequestBody ProductSupplierSaveReqVO createReqVO) {
        return success(productSupplierService.createProductSupplier(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新供应商信息")
    @PreAuthorize("@ss.hasPermission('dm:product-supplier:update')")
    public CommonResult<Boolean> updateProductSupplier(@Valid @RequestBody ProductSupplierSaveReqVO updateReqVO) {
        productSupplierService.updateProductSupplier(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除供应商信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:product-supplier:delete')")
    public CommonResult<Boolean> deleteProductSupplier(@RequestParam("id") Long id) {
        productSupplierService.deleteProductSupplier(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得供应商信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:product-supplier:query')")
    public CommonResult<ProductSupplierRespVO> getProductSupplier(@RequestParam("id") Long id) {
        ProductSupplierDO productSupplier = productSupplierService.getProductSupplier(id);
        return success(BeanUtils.toBean(productSupplier, ProductSupplierRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得供应商信息分页")
    @PreAuthorize("@ss.hasPermission('dm:product-supplier:query')")
    public CommonResult<PageResult<ProductSupplierRespVO>> getProductSupplierPage(@Valid ProductSupplierPageReqVO pageReqVO) {
        PageResult<ProductSupplierDO> pageResult = productSupplierService.getProductSupplierPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProductSupplierRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出供应商信息 Excel")
    @PreAuthorize("@ss.hasPermission('dm:product-supplier:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductSupplierExcel(@Valid ProductSupplierPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProductSupplierDO> list = productSupplierService.getProductSupplierPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "供应商信息.xls", "数据", ProductSupplierRespVO.class,
                        BeanUtils.toBean(list, ProductSupplierRespVO.class));
    }

}