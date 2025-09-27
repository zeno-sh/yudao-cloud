package cn.iocoder.yudao.module.dm.controller.admin.purchase.log;

import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogListSaveReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogRespVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogSaveReqVO;
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

import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog.PurchaseOrderArrivedLogDO;
import cn.iocoder.yudao.module.dm.service.purchase.log.PurchaseOrderArrivedLogService;

@Tag(name = "管理后台 - 采购单到货日志")
@RestController
@RequestMapping("/dm/purchase-order-arrived-log")
@Validated
public class PurchaseOrderArrivedLogController {

    @Resource
    private PurchaseOrderArrivedLogService purchaseOrderArrivedLogService;

    @PostMapping("/create")
    @Operation(summary = "创建采购单到货日志")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order-arrived-log:create')")
    public CommonResult<Long> createPurchaseOrderArrivedLog(@Valid @RequestBody PurchaseOrderArrivedLogSaveReqVO createReqVO) {
        return success(purchaseOrderArrivedLogService.createPurchaseOrderArrivedLog(createReqVO));
    }

    @PostMapping("/batch-create")
    @Operation(summary = "创建采购单到货日志")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order-arrived-log:create')")
    public CommonResult<Boolean> batchCreatePurchaseOrderArrivedLog(@Valid @RequestBody PurchaseOrderArrivedLogListSaveReqVO arrivedLogListSaveReqVO) {
        return success(purchaseOrderArrivedLogService.batchCreatePurchaseOrderArrivedLog(arrivedLogListSaveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购单到货日志")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order-arrived-log:update')")
    public CommonResult<Boolean> updatePurchaseOrderArrivedLog(@Valid @RequestBody PurchaseOrderArrivedLogSaveReqVO updateReqVO) {
        purchaseOrderArrivedLogService.updatePurchaseOrderArrivedLog(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购单到货日志")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:purchase-order-arrived-log:delete')")
    public CommonResult<Boolean> deletePurchaseOrderArrivedLog(@RequestParam("id") Long id) {
        purchaseOrderArrivedLogService.deletePurchaseOrderArrivedLog(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购单到货日志")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order-arrived-log:query')")
    public CommonResult<PurchaseOrderArrivedLogRespVO> getPurchaseOrderArrivedLog(@RequestParam("id") Long id) {
        PurchaseOrderArrivedLogDO purchaseOrderArrivedLog = purchaseOrderArrivedLogService.getPurchaseOrderArrivedLog(id);
        return success(BeanUtils.toBean(purchaseOrderArrivedLog, PurchaseOrderArrivedLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购单到货日志分页")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order-arrived-log:query')")
    public CommonResult<PageResult<PurchaseOrderArrivedLogRespVO>> getPurchaseOrderArrivedLogPage(@Valid PurchaseOrderArrivedLogPageReqVO pageReqVO) {
        PageResult<PurchaseOrderArrivedLogDO> pageResult = purchaseOrderArrivedLogService.getPurchaseOrderArrivedLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PurchaseOrderArrivedLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购单到货日志 Excel")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order-arrived-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseOrderArrivedLogExcel(@Valid PurchaseOrderArrivedLogPageReqVO pageReqVO,
                                                   HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PurchaseOrderArrivedLogDO> list = purchaseOrderArrivedLogService.getPurchaseOrderArrivedLogPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "采购单到货日志.xls", "数据", PurchaseOrderArrivedLogRespVO.class,
                BeanUtils.toBean(list, PurchaseOrderArrivedLogRespVO.class));
    }

}