package cn.iocoder.yudao.module.dm.controller.admin.brand;

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

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.dm.controller.admin.brand.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.brand.DmProductBrandDO;
import cn.iocoder.yudao.module.dm.service.brand.DmProductBrandService;

@Tag(name = "管理后台 - 品牌信息")
@RestController
@RequestMapping("/dm/product-brand")
@Validated
public class DmProductBrandController {

    @Resource
    private DmProductBrandService dmProductBrandService;

    @PostMapping("/create")
    @Operation(summary = "创建品牌信息")
    @PreAuthorize("@ss.hasPermission('dm:product-brand:create')")
    public CommonResult<Long> createProductBrand(@Valid @RequestBody DmProductBrandSaveReqVO createReqVO) {
        return success(dmProductBrandService.createProductBrand(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新品牌信息")
    @PreAuthorize("@ss.hasPermission('dm:product-brand:update')")
    public CommonResult<Boolean> updateProductBrand(@Valid @RequestBody DmProductBrandSaveReqVO updateReqVO) {
        dmProductBrandService.updateProductBrand(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除品牌信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:product-brand:delete')")
    public CommonResult<Boolean> deleteProductBrand(@RequestParam("id") Long id) {
        dmProductBrandService.deleteProductBrand(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得品牌信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:product-brand:query')")
    public CommonResult<DmProductBrandRespVO> getProductBrand(@RequestParam("id") Long id) {
        DmProductBrandDO productBrand = dmProductBrandService.getProductBrand(id);
        return success(BeanUtils.toBean(productBrand, DmProductBrandRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得品牌信息分页")
    @PreAuthorize("@ss.hasPermission('dm:product-brand:query')")
    public CommonResult<PageResult<DmProductBrandRespVO>> getProductBrandPage(@Valid DmProductBrandPageReqVO pageReqVO) {
        PageResult<DmProductBrandDO> pageResult = dmProductBrandService.getProductBrandPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, DmProductBrandRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出品牌信息 Excel")
    @PreAuthorize("@ss.hasPermission('dm:product-brand:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductBrandExcel(@Valid DmProductBrandPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<DmProductBrandDO> list = dmProductBrandService.getProductBrandPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "品牌信息.xls", "数据", DmProductBrandRespVO.class,
                        BeanUtils.toBean(list, DmProductBrandRespVO.class));
    }

}