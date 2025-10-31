package cn.iocoder.yudao.module.dm.controller.admin.order;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsPushOrderLogDO;
import cn.iocoder.yudao.module.dm.dto.FbsPushOrderResponse;
import cn.iocoder.yudao.module.dm.enums.FbsPushOrderStatusEnum;
import cn.iocoder.yudao.module.dm.infrastructure.AuthShopMappingService;
import cn.iocoder.yudao.module.dm.infrastructure.fbs.WarehouseService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.OrderManagerService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import cn.iocoder.yudao.module.dm.infrastructure.wb.WbOrderStatusSyncService;
import cn.iocoder.yudao.module.dm.service.online.OzonProductOnlineService;
import cn.iocoder.yudao.module.dm.service.order.OzonOrderService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsPushOrderLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;

@Tag(name = "管理后台 - Ozon订单")
@RestController
@RequestMapping("/dm/ozon-order")
@Validated
public class OzonOrderController {

    @Resource
    private OzonOrderService ozonOrderService;
    @Resource
    private OrderManagerService orderManagerService;
    @Resource
    private OzonProductOnlineService ozonProductOnlineService;
    @Resource
    private AuthShopMappingService authShopMappingService;
    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private WbOrderStatusSyncService wbOrderStatusSyncService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private FbsPushOrderLogService fbsPushOrderLogService;

    private static final List<String> CANCELED_ORDER_STATUS = Lists.newArrayList("cancelled_from_split_pending", "cancelled", "declined_by_client");


    @PostMapping("/create")
    @Operation(summary = "创建Ozon订单")
    @PreAuthorize("@ss.hasPermission('dm:ozon-order:create')")
    public CommonResult<Long> createOzonOrder(@Valid @RequestBody OzonOrderSaveReqVO createReqVO) {
        return success(ozonOrderService.createOzonOrder(createReqVO));
    }

//    @PutMapping("/update")
//    @Operation(summary = "更新Ozon订单")
//    @PreAuthorize("@ss.hasPermission('dm:ozon-order:update')")
//    public CommonResult<Boolean> updateOzonOrder(@Valid @RequestBody OzonOrderSaveReqVO updateReqVO) {
//        ozonOrderService.updateOzonOrder(updateReqVO);
//        return success(true);
//    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Ozon订单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:ozon-order:delete')")
    public CommonResult<Boolean> deleteOzonOrder(@RequestParam("id") Long id) {
        ozonOrderService.deleteOzonOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Ozon订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:ozon-order:query')")
    public CommonResult<OzonOrderRespVO> getOzonOrder(@RequestParam("id") Long id) {
        OzonOrderDO ozonOrder = ozonOrderService.getOzonOrder(id);
        return success(BeanUtils.toBean(ozonOrder, OzonOrderRespVO.class));
    }


    @PostMapping("/sync")
    @Operation(summary = "同步Ozon订单")
    @DataPermission(enable = false)
    public CommonResult<String> syncOzonOrder(@Valid @RequestBody OzonOrderSyncReqVO ozonOrderSyncReqVO) {

        String begin = ozonOrderSyncReqVO.getInProcessAt()[0];
        String end = ozonOrderSyncReqVO.getInProcessAt()[1];

        long limitDay = DateUtil.betweenDay(DateUtil.parseDate(begin), DateUtil.parseDate(end), true);
        if (limitDay > 31) {
            throw ServiceExceptionUtil.invalidParamException("同步时间不能超过31天");
        }

        String beginDate = DateUtil.format(DateUtil.parseDate(ozonOrderSyncReqVO.getInProcessAt()[0]), DatePattern.UTC_PATTERN);
        String endDate = DateUtil.format(DateUtil.parseDate(ozonOrderSyncReqVO.getInProcessAt()[1]), DatePattern.UTC_PATTERN);

        String[] clientIds = getClientIds(ozonOrderSyncReqVO.getClientIds());
        for (String clientId : clientIds) {
            orderManagerService.syncOrder(clientId, beginDate, endDate);
            wbOrderStatusSyncService.sync(clientId, begin, end);
        }
        return success("同步成功");
    }

    @GetMapping("/page")
    @Operation(summary = "获得Ozon订单分页")
    @PreAuthorize("@ss.hasPermission('dm:ozon-order:query')")
    public CommonResult<PageResult<OzonOrderRespVO>> getOzonOrderPage(@Valid OzonOrderPageReqVO pageReqVO) {

        String[] clientIds = getClientIds(pageReqVO.getClientIds());
        pageReqVO.setClientIds(clientIds);

//        PageResult<OzonOrderDO> pageResult = ozonOrderService.getOzonOrderPage(pageReqVO);
        IPage<OzonOrderDO> pageResult = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        ozonOrderService.getOzonOrderPage2(pageResult, pageReqVO);

        List<OzonOrderDO> orderDOList = pageResult.getRecords();
        if (CollectionUtils.isEmpty(orderDOList)) {
            return success(new PageResult<>(Collections.emptyList(), pageResult.getTotal()));
        }
        PageResult<OzonOrderRespVO> newResult = new PageResult<>();

        // 这里以 发货编号 为唯一键，首先需要以此为发货单位，其次有状态
        List<String> postingNumbers = convertList(orderDOList, OzonOrderDO::getPostingNumber);
        List<OzonOrderItemDO> orderItemDOList = ozonOrderService.batchOrderItemListByPostingNumbers(clientIds, postingNumbers);
        List<String> offerIds = convertList(orderItemDOList, OzonOrderItemDO::getOfferId);
        List<OzonProductOnlineDO> productOnlineDOList = ozonProductOnlineService.batchOzonProductOnlineByOfferId(offerIds);
        List<FbsPushOrderLogDO> fbsPushOrderLogList = fbsPushOrderLogService.getFbsPushOrderLogByPostingNumbers(postingNumbers);
        List<OzonShopMappingDO> ozonShopMappingDOList = ozonShopMappingService.batchShopListByClientIds(Arrays.asList(clientIds));

        Map<String, List<OzonOrderItemDO>> itemListMap = convertMultiMap(orderItemDOList, OzonOrderItemDO::getPostingNumber);
        Map<String, OzonProductOnlineDO> productOnlineDOMap = convertMap(productOnlineDOList, OzonProductOnlineDO::getOfferId);
        Map<String, List<FbsPushOrderLogDO>> pushLogMap = convertMultiMap(fbsPushOrderLogList, FbsPushOrderLogDO::getPostingNumber);
        Map<String, OzonShopMappingDO> shopMappingDOMap = convertMap(ozonShopMappingDOList, OzonShopMappingDO::getClientId);

        List<OzonOrderRespVO> orderRespVOList = new ArrayList<>();
        for (OzonOrderDO ozonOrderDO : orderDOList) {
            OzonOrderRespVO orderRespVO = BeanUtils.toBean(ozonOrderDO, OzonOrderRespVO.class, vo -> {
                if (vo.getInProcessAt() != null) {
                    vo.setInProcessAt(DmDateUtils.convertUtcToMoscowLocalDateTime(vo.getInProcessAt()));
                }
                if (vo.getShipmentDate() != null) {
                    vo.setShipmentDate(DmDateUtils.convertUtcToMoscowLocalDateTime(vo.getShipmentDate()));
                }
            });
            List<OzonOrderItemDO> itemDOList = itemListMap.get(ozonOrderDO.getPostingNumber()).stream()
                    .filter(ozonOrderItemDO -> ozonOrderItemDO.getClientId().equals(ozonOrderDO.getClientId())).collect(Collectors.toList());

            List<OzonOrderItemRespVO> itemVOList = BeanUtils.toBean(itemDOList, OzonOrderItemRespVO.class);

            if (CollectionUtils.isNotEmpty(itemDOList)) {
                for (OzonOrderItemRespVO item : itemVOList) {
                    OzonProductOnlineDO ozonProductOnlineDO = productOnlineDOMap.get(item.getOfferId());
                    if (null != ozonProductOnlineDO) {
                        item.setImage(ozonProductOnlineDO.getImage());
                    }
                }
            }

            MapUtils.findAndThen(shopMappingDOMap, ozonOrderDO.getClientId(), ozonShopMappingDO -> {
                orderRespVO.setShopName(ozonShopMappingDO.getShopName());
                orderRespVO.setPlatform(ozonShopMappingDO.getPlatform());
            });
            handlePush(ozonOrderDO, orderRespVO, pushLogMap);

            orderRespVO.setItems(itemVOList);
            orderRespVOList.add(orderRespVO);
        }
        newResult.setList(orderRespVOList);
        newResult.setTotal(pageResult.getTotal());
        return success(newResult);
    }

    private void handlePush(OzonOrderDO ozonOrderDO, OzonOrderRespVO orderRespVO, Map<String, List<FbsPushOrderLogDO>> pushLogMap) {
        List<FbsPushOrderLogDO> historyPushLogList = pushLogMap.get(ozonOrderDO.getPostingNumber());
        orderRespVO.setPushStatus(FbsPushOrderStatusEnum.NOT_PUSHED.getStatus());

        // FBO FBW无需推送
        if (!ozonOrderDO.getOrderType().equals(20)) {
            orderRespVO.setPushStatus(FbsPushOrderStatusEnum.NONE.getStatus());
            return;
        }

        // 取消的订单无需推送
        if (CANCELED_ORDER_STATUS.contains(ozonOrderDO.getStatus())) {
            orderRespVO.setPushStatus(FbsPushOrderStatusEnum.NONE.getStatus());
            return;
        }

        if (CollectionUtils.isNotEmpty(historyPushLogList)) {
            boolean successPush = historyPushLogList.stream().anyMatch(log -> Boolean.TRUE.equals(log.getStatus()));
            if (successPush) {
                orderRespVO.setPushStatus(FbsPushOrderStatusEnum.SUCCESS.getStatus());
            } else {
                orderRespVO.setPushStatus(FbsPushOrderStatusEnum.FAILED.getStatus());
            }
            List<FbsPushInfoVO> pushInfoVOList = new ArrayList<>();
            for (FbsPushOrderLogDO log : historyPushLogList) {
                FbsPushInfoVO infoVO = new FbsPushInfoVO();
                infoVO.setMessage(log.getResponse());
                infoVO.setPushDateTime(log.getCreateTime());
                infoVO.setSuccess(log.getStatus());
                infoVO.setRequest(log.getRequest());
                pushInfoVOList.add(infoVO);
            }
            orderRespVO.setPushInfo(pushInfoVOList);
        }
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Ozon订单 Excel")
    @PreAuthorize("@ss.hasPermission('dm:ozon-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOzonOrderExcel(@Valid OzonOrderPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        // 设置为不分页，获取所有数据
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        // 直接复用查询接口的逻辑，获取处理后的数据
        PageResult<OzonOrderRespVO> pageResult = getOzonOrderPage(pageReqVO).getData();
        
        // 收集所有订单项并添加订单信息
        List<OzonOrderItemExportVO> exportItems = new ArrayList<>();
        for (OzonOrderRespVO order : pageResult.getList()) {
            if (CollectionUtils.isNotEmpty(order.getItems())) {
                for (OzonOrderItemRespVO item : order.getItems()) {
                    OzonOrderItemExportVO exportVO = new OzonOrderItemExportVO();
                    
                    // 设置订单信息
                    exportVO.setShopName(order.getShopName());
                    exportVO.setPlatform(order.getPlatform());
                    exportVO.setPostingNumber(order.getPostingNumber());
                    exportVO.setStatus(order.getStatus());
                    exportVO.setInProcessAt(order.getInProcessAt());
                    exportVO.setOrderType(order.getOrderType());
                    
                    // 设置订单项信息
                    exportVO.setClientId(item.getClientId());
                    exportVO.setOrderId(item.getOrderId());
                    exportVO.setOfferId(item.getOfferId());
                    exportVO.setQuantity(item.getQuantity());
                    exportVO.setPrice(item.getPrice());
                    exportVO.setImage(item.getImage());
                    
                    exportItems.add(exportVO);
                }
            }
        }
        
        // 导出 Excel
        ExcelUtils.write(response, "订单数据.xls", "订单明细", OzonOrderItemExportVO.class, exportItems);
    }

    // ==================== 子表（订单商品详情） ====================

    @GetMapping("/ozon-order-item/get")
    @Operation(summary = "获得订单商品详情列表")
    @Parameter(name = "orderId", description = "订单号")
    @PreAuthorize("@ss.hasPermission('dm:ozon-order:query')")
    public CommonResult<List<OzonOrderItemDO>> getOzonOrderItem(@RequestParam("clientId") String clientId, @RequestParam("postingNumber") String postingNumber) {
        return success(ozonOrderService.getOrderItemList(clientId, postingNumber, null));
    }

    private String[] getClientIds(String[] clientIds) {
        if (null == clientIds || clientIds.length == 0) {
            List<OzonShopMappingDO> authShopList = authShopMappingService.getAuthShopList();
            return convertList(authShopList, OzonShopMappingDO::getClientId).toArray(new String[0]);
        }
        return clientIds;
    }

    @GetMapping("/push-fbs")
    @Operation(summary = "推送订单")
    @Parameter(name = "id", description = "订单号")
    @PreAuthorize("@ss.hasPermission('dm:ozon-order:query')")
    public CommonResult<FbsPushOrderResponse> pushFbs(@RequestParam("id") Long id) {
        return success(warehouseService.pushOrder(id));
    }

    @PostMapping("/batch-push-fbs")
    @Operation(summary = "推送订单")
    @Parameter(name = "id", description = "订单号")
    @PreAuthorize("@ss.hasPermission('dm:ozon-order:query')")
    public CommonResult<Boolean> batchPushFbs(@RequestBody FbsPushRequest request) {
        warehouseService.batchPushOrder(request.getOrderIds());
        return success(Boolean.TRUE);
    }

    @PostMapping("/update-status")
    @Operation(summary = "更新订单状态")
    @PreAuthorize("@ss.hasPermission('dm:ozon-order:query')")
    public CommonResult<Boolean> batchPushFbs(@RequestBody OzonOrderStatusReqVO reqVO) {
        orderManagerService.updateOrderInfo(reqVO.getClientId(), reqVO.getPostingNumber());
        return success(Boolean.TRUE);
    }
}