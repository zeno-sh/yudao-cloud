package cn.iocoder.yudao.module.dm.controller.admin.commission;

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

import cn.iocoder.yudao.module.dm.controller.admin.commission.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.commission.CategoryCommissionDO;
import cn.iocoder.yudao.module.dm.service.commission.CategoryCommissionService;

@Tag(name = "管理后台 - 类目佣金")
@RestController
@RequestMapping("/dm/category-commission")
@Validated
public class CategoryCommissionController {

    @Resource
    private CategoryCommissionService categoryCommissionService;

    @PostMapping("/create")
    @Operation(summary = "创建类目佣金")
    @PreAuthorize("@ss.hasPermission('dm:category-commission:create')")
    public CommonResult<Long> createCategoryCommission(@Valid @RequestBody CategoryCommissionSaveReqVO createReqVO) {
        return success(categoryCommissionService.createCategoryCommission(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新类目佣金")
    @PreAuthorize("@ss.hasPermission('dm:category-commission:update')")
    public CommonResult<Boolean> updateCategoryCommission(@Valid @RequestBody CategoryCommissionSaveReqVO updateReqVO) {
        categoryCommissionService.updateCategoryCommission(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除类目佣金")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:category-commission:delete')")
    public CommonResult<Boolean> deleteCategoryCommission(@RequestParam("id") Long id) {
        categoryCommissionService.deleteCategoryCommission(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得类目佣金")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:category-commission:query')")
    public CommonResult<CategoryCommissionRespVO> getCategoryCommission(@RequestParam("id") Long id) {
        CategoryCommissionDO categoryCommission = categoryCommissionService.getCategoryCommission(id);
        return success(BeanUtils.toBean(categoryCommission, CategoryCommissionRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得类目佣金列表")
    @PreAuthorize("@ss.hasPermission('dm:category-commission:query')")
    public CommonResult<List<CategoryCommissionRespVO>> getCategoryCommissionList(@Valid CategoryCommissionListReqVO listReqVO) {
        List<CategoryCommissionDO> list = categoryCommissionService.getCategoryCommissionList(listReqVO);
        return success(BeanUtils.toBean(list, CategoryCommissionRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出类目佣金 Excel")
    @PreAuthorize("@ss.hasPermission('dm:category-commission:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCategoryCommissionExcel(@Valid CategoryCommissionListReqVO listReqVO,
                                              HttpServletResponse response) throws IOException {
        List<CategoryCommissionDO> list = categoryCommissionService.getCategoryCommissionList(listReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "类目佣金.xls", "数据", CategoryCommissionRespVO.class,
                BeanUtils.toBean(list, CategoryCommissionRespVO.class));
    }

    @GetMapping("/tree")
    @ApiAccessLog(operateType = GET)
    public CommonResult<List<CategoryCommissionTreeRespVO>> getCategoryCommissionTree()  {
        List<CategoryCommissionTreeRespVO> categoryCommissionTree = categoryCommissionService.getCategoryCommissionTree();
        return success(categoryCommissionTree);
    }

}