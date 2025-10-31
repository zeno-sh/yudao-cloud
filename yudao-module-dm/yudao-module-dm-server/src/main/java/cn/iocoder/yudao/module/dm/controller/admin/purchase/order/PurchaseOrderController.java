package cn.iocoder.yudao.module.dm.controller.admin.purchase.order;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogRespVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog.PurchaseOrderArrivedLogDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.supplier.ProductSupplierDO;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseOrderManagerService;
import cn.iocoder.yudao.module.dm.service.supplier.ProductSupplierService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

import cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderItemDO;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseOrderService;

@Tag(name = "管理后台 - 采购单")
@RestController
@RequestMapping("/dm/purchase-order")
@Validated
public class PurchaseOrderController {

    @Resource
    private PurchaseOrderService purchaseOrderService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private ProductSupplierService productSupplierService;
    @Resource
    private PurchaseOrderManagerService purchaseOrderManagerService;
    @Resource
    private ProductInfoService productInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建采购单")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:create')")
    public CommonResult<Long> createPurchaseOrder(@Valid @RequestBody PurchaseOrderSaveReqVO createReqVO) {
        return success(purchaseOrderService.createPurchaseOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购单")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:update')")
    public CommonResult<Boolean> updatePurchaseOrder(@Valid @RequestBody PurchaseOrderSaveReqVO updateReqVO) {
        purchaseOrderService.updatePurchaseOrder(updateReqVO);
        return success(true);
    }

    @PutMapping("/force-update")
    @Operation(summary = "更新采购单")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:force-update')")
    public CommonResult<Boolean> forceUpdatePurchaseOrder(@Valid @RequestBody PurchaseForceUpdateVO forceUpdateVO) {
        purchaseOrderService.forceUpdatePurchaseOrder(forceUpdateVO.getPurchaseOrderId(), forceUpdateVO.getPurchaseOrderItemId());
        return success(true);
    }

//    @PutMapping("/update-arrive")
//    @Operation(summary = "更新到货")
//    @PreAuthorize("@ss.hasPermission('dm:purchase-order:update')")
//    public CommonResult<Boolean> updatePurchaseOrderArrive(@Valid @RequestBody PurchaseArriveSaveReqVO purchaseArriveSaveReqVO) {
//        purchaseOrderService.updateArriveQuantity(purchaseArriveSaveReqVO);
//        return success(true);
//    }

    @PutMapping("/updateStatus")
    @Operation(summary = "更新采购单")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:update')")
    public CommonResult<Boolean> updatePurchaseOrderStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        purchaseOrderService.updatePurchaseOrderStatus(id, status);
        return success(true);
    }

    @PostMapping("/update-remark")
    @Operation(summary = "更新采购单备注")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:update')")
    public CommonResult<Boolean> updatePurchaseOrderRemark(@RequestBody PurchaseOrderSaveReqVO updateReqVO) {
        purchaseOrderService.updatePurchaseOrderRemark(updateReqVO.getId(), updateReqVO.getRemark());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:delete')")
    public CommonResult<Boolean> deletePurchaseOrder(@RequestParam("id") Long id) {
        purchaseOrderService.deletePurchaseOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:query')")
    public CommonResult<PurchaseOrderRespVO> getPurchaseOrder(@RequestParam("orderId") Long orderId) {
        PurchaseOrderDO purchaseOrder = purchaseOrderService.getPurchaseOrder(orderId);
        Long userId = Objects.isNull(purchaseOrder.getOwner()) ? Long.valueOf(purchaseOrder.getCreator()) : purchaseOrder.getOwner();
        AdminUserRespDTO user = adminUserApi.getUser(userId).getCheckedData();
        return success(BeanUtils.toBean(purchaseOrder, PurchaseOrderRespVO.class, vo -> {
            vo.setOwnerName(user.getNickname());
        }));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购单分页")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:query')")
    public CommonResult<PageResult<PurchaseOrderRespVO>> getPurchaseOrderPage(@Valid PurchaseOrderPageReqVO pageReqVO) {
//        PageResult<PurchaseOrderDO> pageResult = purchaseOrderService.getPurchaseOrderPage(pageReqVO);
        IPage<PurchaseOrderDO> pageResult = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());

        pageResult = purchaseOrderService.getPurchaseOrderPage2(pageResult, pageReqVO);

        // 这里更新一次采购单状态
        purchaseOrderService.batchUpdatePurchaseOrderStatus(pageResult.getRecords());

        // 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(pageResult.getRecords(),
                purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator()),
                        purchaseOrderDO.getOwner())));

        Map<Long, ProductSupplierDO> supplierMap = productSupplierService.getSupplierMap(convertListByFlatMap(pageResult.getRecords(),
                purchaseOrderDO -> Stream.of(purchaseOrderDO.getSupplierId())));

        List<PurchaseOrderDO> purchaseOrderDOList = pageResult.getRecords();
        Map<Long, LocalDateTime> allivalDateMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(purchaseOrderDOList)) {
            List<Long> orderIds = convertList(purchaseOrderDOList, PurchaseOrderDO::getId);
            List<PurchaseOrderItemDO> purchaseOrderItemDOList = purchaseOrderService.batchPurchaseOrderItemListByOrderIds(orderIds);

            // 选择每个 orderId 对应的最近的 arrivalDate
            allivalDateMap.putAll(purchaseOrderItemDOList.stream()
                    .collect(Collectors.groupingBy(
                            PurchaseOrderItemDO::getOrderId,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    items -> items.stream()
                                            .map(PurchaseOrderItemDO::getArrivalDate)
                                            .filter(Objects::nonNull)  // 过滤掉 null 的 arrivalDate
                                            .min(Comparator.comparingLong(arrivalDate ->
                                                    Math.abs(ChronoUnit.DAYS.between(arrivalDate, LocalDateTime.now()))
                                            ))
                                            .orElse(null)
                            )
                    )));
        }


        List<PurchaseOrderRespVO> orderRespVOS = BeanUtils.toBean(pageResult.getRecords(), PurchaseOrderRespVO.class, vo -> {
            MapUtils.findAndThen(userMap, vo.getOwner().longValue(), user -> vo.setOwnerName(user.getNickname()));
            MapUtils.findAndThen(userMap, Long.parseLong(vo.getCreator()), user -> vo.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(supplierMap, vo.getSupplierId(), supplier -> vo.setSupplierName(supplier.getSupplierName()));
            MapUtils.findAndThen(allivalDateMap, vo.getId(), vo::setArrivalDate);
        });

        return success(new PageResult<>(orderRespVOS, pageResult.getTotal()));
    }

    @GetMapping("/export-excel1")
    @Operation(summary = "导出采购单 Excel")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseOrderExcelOld(@Valid PurchaseOrderPageReqVO pageReqVO,
                                            HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<PurchaseOrderDO> list = purchaseOrderService.getPurchaseOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "采购单.xls", "数据", PurchaseOrderRespVO.class,
                BeanUtils.toBean(list, PurchaseOrderRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购单 Excel")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseOrderExcel(@RequestParam("purchaseOrderId") Long purchaseOrderId,
                                         HttpServletResponse response) throws IOException {
        PurchaseOrderDO purchaseOrder = purchaseOrderService.getPurchaseOrder(purchaseOrderId);

        List<PurchaseOrderItemDO> purchaseOrderItemDOList = purchaseOrderService.getPurchaseOrderItemListByOrderId(purchaseOrderId);

        List<Long> productIds = convertList(purchaseOrderItemDOList, PurchaseOrderItemDO::getProductId);
        Map<Long, ProductSimpleInfoVO> productSimpleInfoVOMap = productInfoService.batchQueryProductSimpleInfo(productIds);

        LinkedHashMap<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put("采购单号：", purchaseOrder.getOrderNo());
        headerMap.put("合同总金额：", String.valueOf(purchaseOrder.getTotalPrice()));
        headerMap.put("已支付：", String.valueOf(purchaseOrder.getTotalPrice()));
        headerMap.put("未支付：", String.valueOf(purchaseOrder.getTotalPrice().subtract(purchaseOrder.getPaymentPrice())));
        headerMap.put("单据负责人：", adminUserApi.getUser(purchaseOrder.getOwner()).getCheckedData().getNickname());
        headerMap.put("单据日期：", DateUtil.format(purchaseOrder.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN));
        // 导出 Excel
        ExcelUtils.write(response, "采购单明细.xls", "数据", PurchaseOrderItemVO.class,
                BeanUtils.toBean(purchaseOrderItemDOList, PurchaseOrderItemVO.class, vo ->
                        vo.setSku(productSimpleInfoVOMap.get(vo.getProductId()).getSkuId())),
                headerMap);
    }

    // ==================== 子表（采购单明细） ====================

    @GetMapping("/purchase-order-item/list-by-order-id")
    @Operation(summary = "获得采购单明细列表")
    @Parameter(name = "orderId", description = "采购单Id")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:query')")
    public CommonResult<List<PurchaseOrderItemVO>> getPurchaseOrderItemListByOrderId(@RequestParam("orderId") Long orderId) {
        return success(purchaseOrderManagerService.buildPurchaseOrderItemVO(orderId, purchaseOrderService.getPurchaseOrderItemListByOrderId(orderId)));
    }


    @GetMapping("/item-page")
    @Operation(summary = "获得采购单分页")
    @PreAuthorize("@ss.hasPermission('dm:purchase-order:query')")
    public CommonResult<PageResult<PurchaseOrderItemVO>> getPurchaseOrderItemPage(@Valid PurchaseOrderItemPageReqVO pageReqVO) {
        PageResult<PurchaseOrderItemDO> pageResult = purchaseOrderService.getPurchaseOrderItemPage(pageReqVO);
        List<PurchaseOrderItemVO> purchaseOrderItemVOList = purchaseOrderManagerService.buildPurchaseOrderItemVO(pageResult.getList());

        PageResult<PurchaseOrderItemVO> newPageResult = new PageResult<>();
        newPageResult.setTotal(pageResult.getTotal());
        newPageResult.setList(purchaseOrderItemVOList);

        return success(newPageResult);
    }

    // ==================== 子表（采购单到货日志） ====================

    @GetMapping("/purchase-order-arrived-log/list-by-purchase-order-id")
    @Operation(summary = "获得采购单到货日志列表")
    @Parameter(name = "purchaseOrderId", description = "采购单ID")
//    @PreAuthorize("@ss.hasPermission('dm:purchase-order:query')")
    @DataPermission(enable = false)
    public CommonResult<List<PurchaseOrderArrivedLogRespVO>> getPurchaseOrderArrivedLogListByPurchaseOrderId(@RequestParam("purchaseOrderId") Long purchaseOrderId) {
        List<PurchaseOrderArrivedLogDO> purchaseOrderArrivedLogList = purchaseOrderService.getPurchaseOrderArrivedLogListByPurchaseOrderId(purchaseOrderId);
        if (CollectionUtils.isEmpty(purchaseOrderArrivedLogList)) {
            return success(Collections.emptyList());
        }
        // 过滤0的到货记录
        purchaseOrderArrivedLogList = purchaseOrderArrivedLogList.stream()
                .filter(purchaseOrderArrivedLogDO -> purchaseOrderArrivedLogDO.getArrivedQuantity() != 0)
                .collect(Collectors.toList());
        return success(buildPurchaseOrderArrivedLogRespVO(purchaseOrderId, purchaseOrderArrivedLogList));
    }

    private List<PurchaseOrderArrivedLogRespVO> buildPurchaseOrderArrivedLogRespVO(Long purchaseOrderId, List<PurchaseOrderArrivedLogDO> purchaseOrderArrivedLogList) {
        // 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(purchaseOrderArrivedLogList,
                purchaseOrderArrivedLogDO -> Stream.of(NumberUtils.parseLong(purchaseOrderArrivedLogDO.getCreator()))));

        Map<Long, PurchaseOrderItemDO> purchaseOrderItemDOMap = convertMap(purchaseOrderService.getPurchaseOrderItemListByOrderId(purchaseOrderId), PurchaseOrderItemDO::getId);

        List<PurchaseOrderArrivedLogRespVO> arrivedLogRespVOList = BeanUtils.toBean(purchaseOrderArrivedLogList, PurchaseOrderArrivedLogRespVO.class, purchaseOrderArrivedLog -> {
            MapUtils.findAndThen(userMap, Long.parseLong(purchaseOrderArrivedLog.getCreator()), user -> purchaseOrderArrivedLog.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(purchaseOrderItemDOMap, purchaseOrderArrivedLog.getPurchaseOrderItemId(), purchaseOrderItemDO -> {
                purchaseOrderArrivedLog.setProductId(purchaseOrderItemDO.getProductId());
                purchaseOrderArrivedLog.setPlanNumber(purchaseOrderItemDO.getPlanNumber());
            });
        });

        arrivedLogRespVOList.sort(Comparator.comparing(PurchaseOrderArrivedLogRespVO::getProductId));
        return arrivedLogRespVOList;
    }

}