package cn.iocoder.yudao.module.dm.controller.admin.logistics;

import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseDO;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsWarehouseService;
import org.apache.commons.collections4.CollectionUtils;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

import cn.iocoder.yudao.module.dm.controller.admin.logistics.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeServicesDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeDetailDO;
import cn.iocoder.yudao.module.dm.service.logistics.FbsFeeServicesService;

@Tag(name = "管理后台 - 收费项目")
@RestController
@RequestMapping("/dm/fbs-fee-services")
@Validated
public class FbsFeeServicesController {

    @Resource
    private FbsFeeServicesService fbsFeeServicesService;
    @Resource
    private FbsWarehouseService fbsWarehouseService;

    @PostMapping("/create")
    @Operation(summary = "创建收费项目")
    @PreAuthorize("@ss.hasPermission('dm:fbs-fee-services:create')")
    public CommonResult<Long> createFbsFeeServices(@Valid @RequestBody FbsFeeServicesSaveReqVO createReqVO) {
        return success(fbsFeeServicesService.createFbsFeeServices(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新收费项目")
    @PreAuthorize("@ss.hasPermission('dm:fbs-fee-services:update')")
    public CommonResult<Boolean> updateFbsFeeServices(@Valid @RequestBody FbsFeeServicesSaveReqVO updateReqVO) {
        fbsFeeServicesService.updateFbsFeeServices(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除收费项目")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:fbs-fee-services:delete')")
    public CommonResult<Boolean> deleteFbsFeeServices(@RequestParam("id") Long id) {
        fbsFeeServicesService.deleteFbsFeeServices(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得收费项目")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:fbs-fee-services:query')")
    public CommonResult<FbsFeeServicesRespVO> getFbsFeeServices(@RequestParam("id") Long id) {
        FbsFeeServicesDO fbsFeeServices = fbsFeeServicesService.getFbsFeeServices(id);
        return success(BeanUtils.toBean(fbsFeeServices, FbsFeeServicesRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得收费项目分页")
    @PreAuthorize("@ss.hasPermission('dm:fbs-fee-services:query')")
    public CommonResult<PageResult<FbsFeeServicesRespVO>> getFbsFeeServicesPage(@Valid FbsFeeServicesPageReqVO pageReqVO) {
        PageResult<FbsFeeServicesDO> pageResult = fbsFeeServicesService.getFbsFeeServicesPage(pageReqVO);
        List<FbsFeeServicesDO> servicesDOList = pageResult.getList();
        if (CollectionUtils.isEmpty(servicesDOList)) {
            return success(PageResult.empty());
        }

        List<Long> warehouseIds = convertList(servicesDOList, FbsFeeServicesDO::getWarehouseId);
        List<FbsWarehouseDO> fbsWarehouseDOList = fbsWarehouseService.batchFbsWarehouse(warehouseIds);
        Map<Long, String> warehouseMap = convertMap(fbsWarehouseDOList, FbsWarehouseDO::getId, FbsWarehouseDO::getName);
        return success(BeanUtils.toBean(pageResult, FbsFeeServicesRespVO.class, fbsFeeServicesRespVO -> {
            MapUtils.findAndThen(warehouseMap, fbsFeeServicesRespVO.getWarehouseId(),
                    warehouseName -> fbsFeeServicesRespVO.setWarehouseName(warehouseName));
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出收费项目 Excel")
    @PreAuthorize("@ss.hasPermission('dm:fbs-fee-services:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFbsFeeServicesExcel(@Valid FbsFeeServicesPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FbsFeeServicesDO> list = fbsFeeServicesService.getFbsFeeServicesPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "收费项目.xls", "数据", FbsFeeServicesRespVO.class,
                        BeanUtils.toBean(list, FbsFeeServicesRespVO.class));
    }

    // ==================== 子表（收费明细） ====================

    @GetMapping("/fbs-fee-detail/list-by-service-id")
    @Operation(summary = "获得收费明细列表")
    @Parameter(name = "serviceId", description = "收费项目ID")
    @PreAuthorize("@ss.hasPermission('dm:fbs-fee-services:query')")
    public CommonResult<List<FbsFeeDetailDO>> getFbsFeeDetailListByServiceId(@RequestParam("serviceId") Long serviceId) {
        return success(fbsFeeServicesService.getFbsFeeDetailListByServiceId(serviceId));
    }

}