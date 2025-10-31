package cn.iocoder.yudao.module.dm.controller.admin.category;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.dm.controller.admin.category.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.dmcategory.DmProductCategoryDO;
import cn.iocoder.yudao.module.dm.service.dmcategory.DmProductCategoryService;

@Tag(name = "管理后台 - 产品分类")
@RestController
@RequestMapping("/dm/product-category")
@Validated
public class DmProductCategoryController {

    @Resource
    private DmProductCategoryService productCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建产品分类")
    @PreAuthorize("@ss.hasPermission('dm:product-category:create')")
    public CommonResult<Long> createProductCategory(@Valid @RequestBody DmProductCategorySaveReqVO createReqVO) {
        return success(productCategoryService.createProductCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品分类")
    @PreAuthorize("@ss.hasPermission('dm:product-category:update')")
    public CommonResult<Boolean> updateProductCategory(@Valid @RequestBody DmProductCategorySaveReqVO updateReqVO) {
        productCategoryService.updateProductCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:product-category:delete')")
    public CommonResult<Boolean> deleteProductCategory(@RequestParam("id") Long id) {
        productCategoryService.deleteProductCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:product-category:query')")
    public CommonResult<DmProductCategoryRespVO> getProductCategory(@RequestParam("id") Long id) {
        DmProductCategoryDO productCategory = productCategoryService.getProductCategory(id);
        return success(BeanUtils.toBean(productCategory, DmProductCategoryRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得产品分类列表")
    @PreAuthorize("@ss.hasPermission('dm:product-category:query')")
    public CommonResult<List<DmProductCategoryRespVO>> getProductCategoryList(@Valid DmProductCategoryListReqVO listReqVO) {
        List<DmProductCategoryDO> list = productCategoryService.getProductCategoryList(listReqVO);
        list.sort(Comparator.comparing(DmProductCategoryDO::getSort));
        return success(BeanUtils.toBean(list, DmProductCategoryRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品分类 Excel")
    @PreAuthorize("@ss.hasPermission('dm:product-category:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductCategoryExcel(@Valid DmProductCategoryListReqVO listReqVO,
              HttpServletResponse response) throws IOException {
        List<DmProductCategoryDO> list = productCategoryService.getProductCategoryList(listReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "产品分类.xls", "数据", DmProductCategoryRespVO.class,
                        BeanUtils.toBean(list, DmProductCategoryRespVO.class));
    }

}