package cn.iocoder.yudao.module.dm.controller.admin.warehouse;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.FbsWarehouseManagerService;
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

import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseAuthDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseMappingDO;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsWarehouseService;

@Tag(name = "管理后台 - 海外仓仓库")
@RestController
@RequestMapping("/dm/fbs-warehouse")
@Validated
public class FbsWarehouseController {

    @Resource
    private FbsWarehouseService fbsWarehouseService;
    @Resource
    private FbsWarehouseManagerService fbsWarehouseManagerService;

    @PostMapping("/create")
    @Operation(summary = "创建海外仓仓库")
    @PreAuthorize("@ss.hasPermission('dm:fbs-warehouse:create')")
    public CommonResult<Long> createFbsWarehouse(@Valid @RequestBody FbsWarehouseSaveReqVO createReqVO) {
        return success(fbsWarehouseService.createFbsWarehouse(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新海外仓仓库")
    @PreAuthorize("@ss.hasPermission('dm:fbs-warehouse:update')")
    public CommonResult<Boolean> updateFbsWarehouse(@Valid @RequestBody FbsWarehouseSaveReqVO updateReqVO) {
        fbsWarehouseService.updateFbsWarehouse(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除海外仓仓库")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:fbs-warehouse:delete')")
    public CommonResult<Boolean> deleteFbsWarehouse(@RequestParam("id") Long id) {
        fbsWarehouseService.deleteFbsWarehouse(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得海外仓仓库")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:fbs-warehouse:query')")
    public CommonResult<FbsWarehouseRespVO> getFbsWarehouse(@RequestParam("id") Long id) {
        FbsWarehouseDO fbsWarehouse = fbsWarehouseService.getFbsWarehouse(id);
        return success(BeanUtils.toBean(fbsWarehouse, FbsWarehouseRespVO.class));
    }

    @GetMapping("/get-simple-all")
    @Operation(summary = "获得海外仓仓库")
    @PreAuthorize("@ss.hasPermission('dm:fbs-warehouse:query')")
    public CommonResult<List<FbsWarehouseRespVO>> getFbsWarehouse() {
        List<FbsWarehouseDO> allFbsWarehouse = fbsWarehouseService.getAllFbsWarehouse();
        return success(BeanUtils.toBean(allFbsWarehouse, FbsWarehouseRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得海外仓仓库分页")
    @PreAuthorize("@ss.hasPermission('dm:fbs-warehouse:query')")
    public CommonResult<PageResult<FbsWarehouseRespVO>> getFbsWarehousePage(@Valid FbsWarehousePageReqVO pageReqVO) {
        PageResult<FbsWarehouseDO> pageResult = fbsWarehouseService.getFbsWarehousePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, FbsWarehouseRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出海外仓仓库 Excel")
    @PreAuthorize("@ss.hasPermission('dm:fbs-warehouse:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFbsWarehouseExcel(@Valid FbsWarehousePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FbsWarehouseDO> list = fbsWarehouseService.getFbsWarehousePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "海外仓仓库.xls", "数据", FbsWarehouseRespVO.class,
                        BeanUtils.toBean(list, FbsWarehouseRespVO.class));
    }

    // ==================== 子表（海外仓授权信息） ====================

    @GetMapping("/fbs-warehouse-auth/get-by-warehouse-id")
    @Operation(summary = "获得海外仓授权信息")
    @Parameter(name = "warehouseId", description = "关联的仓库ID")
    @PreAuthorize("@ss.hasPermission('dm:fbs-warehouse:query')")
    public CommonResult<FbsWarehouseAuthDO> getFbsWarehouseAuthByWarehouseId(@RequestParam("warehouseId") Long warehouseId) {
        return success(fbsWarehouseService.getFbsWarehouseAuthByWarehouseId(warehouseId));
    }

    // ==================== 子表（海外仓平台仓映射） ====================

    @GetMapping("/fbs-warehouse-mapping/list-by-warehouse-id")
    @Operation(summary = "获得海外仓平台仓映射列表")
    @Parameter(name = "warehouseId", description = "关联的仓库ID")
    @PreAuthorize("@ss.hasPermission('dm:fbs-warehouse:query')")
    public CommonResult<List<FbsWarehouseMappingDO>> getFbsWarehouseMappingListByWarehouseId(@RequestParam("warehouseId") Long warehouseId) {
        return success(fbsWarehouseService.getFbsWarehouseMappingListByWarehouseId(warehouseId));
    }

    @PostMapping("/fbs-warehouse-mapping/platform-warehouse-list")
    @Operation(summary = "获得平台仓库列表")
    @PreAuthorize("@ss.hasPermission('dm:fbs-warehouse:query')")
    public CommonResult<List<FbsWarehouseMappingDO>> getFbsWarehouseMappingByWarehouseId(@Valid @RequestBody FbsPlatformWarehouseReqVO reqVO) {
        return success(fbsWarehouseManagerService.queryPlatformWarehouseList(reqVO.getClientIds()));
    }

}