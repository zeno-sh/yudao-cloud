package cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderStatsDO;
import cn.iocoder.yudao.module.dm.infrastructure.AuthShopMappingService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.SupplyOrderManagerService;
import org.apache.commons.collections4.CollectionUtils;
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

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.OzonSupplyOrderDO;
import cn.iocoder.yudao.module.dm.service.ozonsupplyorder.OzonSupplyOrderService;

@Tag(name = "管理后台 - 供应订单")
@RestController
@RequestMapping("/dm/ozon-supply-order")
@Validated
public class OzonSupplyOrderController {

    @Resource
    private OzonSupplyOrderService ozonSupplyOrderService;
    @Resource
    private SupplyOrderManagerService supplyOrderManagerService;
    @Resource
    private AuthShopMappingService authShopMappingService;

    @PostMapping("/create")
    @Operation(summary = "创建供应订单")
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:create')")
    public CommonResult<Long> createOzonSupplyOrder(@Valid @RequestBody OzonSupplyOrderSaveReqVO createReqVO) {
        return success(ozonSupplyOrderService.createOzonSupplyOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新供应订单")
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:update')")
    public CommonResult<Boolean> updateOzonSupplyOrder(@Valid @RequestBody OzonSupplyOrderSaveReqVO updateReqVO) {
        ozonSupplyOrderService.updateOzonSupplyOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除供应订单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:delete')")
    public CommonResult<Boolean> deleteOzonSupplyOrder(@RequestParam("id") Long id) {
        ozonSupplyOrderService.deleteOzonSupplyOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得供应订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:query')")
    public CommonResult<OzonSupplyOrderRespVO> getOzonSupplyOrder(@RequestParam("id") Long id) {
        OzonSupplyOrderDO ozonSupplyOrder = ozonSupplyOrderService.getOzonSupplyOrder(id);
        return success(BeanUtils.toBean(ozonSupplyOrder, OzonSupplyOrderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得供应订单分页")
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:query')")
    public CommonResult<PageResult<OzonSupplyOrderRespVO>> getOzonSupplyOrderPage(@Valid OzonSupplyOrderPageReqVO pageReqVO) {
        String[] clientIds = getClientIds(pageReqVO.getClientIds());
        pageReqVO.setClientIds(clientIds);
        // 1. 分页查询
        PageResult<OzonSupplyOrderDO> pageResult = ozonSupplyOrderService.getOzonSupplyOrderPage(pageReqVO);
        if (CollectionUtils.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(Collections.emptyList(), pageResult.getTotal()));
        }

        // 2. 按clientId分组订单ID
        Map<String, List<Long>> clientOrderIdsMap = new HashMap<>();
        for (OzonSupplyOrderDO order : pageResult.getList()) {
            clientOrderIdsMap.computeIfAbsent(order.getClientId(), k -> new ArrayList<>())
                    .add(order.getSupplyOrderId());
        }
        
        // 3. 获取统计数据
        Map<Long, OzonSupplyOrderStatsDO> orderStatsMap = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : clientOrderIdsMap.entrySet()) {
            String clientId = entry.getKey();
            List<Long> orderIds = entry.getValue();
            if (CollectionUtils.isEmpty(orderIds)) {
                continue;
            }
            List<OzonSupplyOrderStatsDO> orderStatsList = ozonSupplyOrderService.getOzonSupplyOrderStats(clientId, orderIds);
            for (OzonSupplyOrderStatsDO stats : orderStatsList) {
                orderStatsMap.put(stats.getSupplyOrderId(), stats);
            }
        }

        // 4. 组装VO
        List<OzonSupplyOrderRespVO> voList = new ArrayList<>(pageResult.getList().size());
        for (OzonSupplyOrderDO order : pageResult.getList()) {
            OzonSupplyOrderRespVO vo = BeanUtils.toBean(order, OzonSupplyOrderRespVO.class);
            // 设置统计数据
            OzonSupplyOrderStatsDO stats = orderStatsMap.get(order.getSupplyOrderId());
            if (stats != null) {
                vo.setTotalItems(stats.getTotalItems())
                        .setTotalVolume(stats.getTotalVolume())
                        .setSkuCount(stats.getSkuCount());
            }
            voList.add(vo);
        }

        return success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出供应订单 Excel")
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOzonSupplyOrderExcel(@Valid OzonSupplyOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OzonSupplyOrderDO> list = ozonSupplyOrderService.getOzonSupplyOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "供应订单.xls", "数据", OzonSupplyOrderRespVO.class,
                        BeanUtils.toBean(list, OzonSupplyOrderRespVO.class));
    }

    // ==================== 子表（供应订单商品） ====================

    @GetMapping("/ozon-supply-order-item/list-by-supply-order-id")
    @Operation(summary = "获得供应订单商品列表")
    @Parameter(name = "supplyOrderId", description = "供应订单ID")
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:query')")
    public CommonResult<List<OzonSupplyOrderItemDO>> getOzonSupplyOrderItemListBySupplyOrderId(@RequestParam("supplyOrderId") Long supplyOrderId) {
        return success(ozonSupplyOrderService.getOzonSupplyOrderItemListBySupplyOrderId(supplyOrderId));
    }

    @PostMapping("/sync")
    @Operation(summary = "手动同步供应订单")
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:export')")
    public CommonResult<Boolean> syncSupplyOrders(@Valid @RequestBody List<String> clientIds) {
        supplyOrderManagerService.asyncSyncSupplyOrders(clientIds);
        return success(true);
    }

    @PostMapping("/sync-items")
    @Operation(summary = "同步指定供应订单的商品信息")
    @Parameter(name = "supplyOrderId", description = "供应订单ID", required = true)
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:export')")
    public CommonResult<Boolean> syncSupplyOrderItems(@RequestParam("supplyOrderId") Long supplyOrderId) {
        ozonSupplyOrderService.syncSupplyOrderItems(supplyOrderId);
        return success(true);
    }

    @GetMapping("/fbo-inbound-report/page")
    @Operation(summary = "获得FBO进仓报表分页")
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:export')")
    public CommonResult<PageResult<OzonFboInboundReportRespVO>> getFboInboundReportPage(@Valid OzonFboInboundReportReqVO pageReqVO) {
        // 1. 处理店铺编号
        String[] clientIds = getClientIds(pageReqVO.getClientIds());
        pageReqVO.setClientIds(clientIds);
        // 2. 查询FBO进仓报表
        return success(ozonSupplyOrderService.getFboInboundReportPage(pageReqVO));
    }

    @GetMapping("/fbo-inbound-report/export")
    @Operation(summary = "导出FBO进仓报表")
    @PreAuthorize("@ss.hasPermission('dm:ozon-supply-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFboInboundReport(@Valid OzonFboInboundReportReqVO reqVO,
                                     HttpServletResponse response) throws IOException {
        // 1. 处理店铺编号
        String[] clientIds = getClientIds(reqVO.getClientIds());
        reqVO.setClientIds(clientIds);
        // 2. 查询数据
        reqVO.setPageNo(1);
        reqVO.setPageSize(Integer.MAX_VALUE);
        List<OzonFboInboundReportRespVO> list = ozonSupplyOrderService.getFboInboundReportPage(reqVO).getList();

        // 3. 转换为Excel VO
        List<OzonFboInboundReportExcelVO> excelList = new ArrayList<>(list.size());
        for (OzonFboInboundReportRespVO item : list) {
            OzonFboInboundReportExcelVO excelVO = new OzonFboInboundReportExcelVO()
                    .setMonth(item.getMonth())
                    .setSkuId(item.getProductSimpleInfo() != null ? item.getProductSimpleInfo().getSkuId() : null)
                    .setProductName(item.getProductSimpleInfo() != null ? item.getProductSimpleInfo().getSkuName() : null)
                    .setInitialBalance(item.getInitialBalance())
                    .setInboundQuantity(item.getInboundQuantity())
                    .setSalesQuantity(item.getSalesQuantity())
                    .setFinalBalance(item.getFinalBalance())
                    .setSupplierPrice(item.getSupplierPrice())
                    .setTaxIncludedAmount(item.getTaxIncludedAmount())
                    .setTaxExcludedAmount(item.getTaxExcludedAmount());
            excelList.add(excelVO);
        }

        // 4. 导出Excel
        ExcelUtils.write(response, "FBO进仓报表.xlsx", "数据", OzonFboInboundReportExcelVO.class, excelList);
    }

    private String[] getClientIds(String[] clientIds) {
        if (null == clientIds || clientIds.length == 0) {
            List<OzonShopMappingDO> authShopList = authShopMappingService.getAuthShopList();
            return convertList(authShopList, OzonShopMappingDO::getClientId).toArray(new String[0]);
        }
        return clientIds;
    }

}