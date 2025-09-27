package cn.iocoder.yudao.module.dm.controller.admin.transport;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.controller.admin.transport.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanDetailDTO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanItemDO;
import cn.iocoder.yudao.module.dm.enums.DictTypeConstant;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.purchase.PurchasePlanService;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseOrderService;
import cn.iocoder.yudao.module.dm.service.transport.TransportPlanService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

@Tag(name = "管理后台 - 头程计划")
@RestController
@RequestMapping("/dm/transport-plan")
@Validated
public class TransportPlanController {

    @Resource
    private TransportPlanService transportPlanService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private ProductInfoService productInfoService;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;
    @Resource
    private DictDataApi dictDataApi;

    @PostMapping("/create")
    @Operation(summary = "创建头程计划")
    @PreAuthorize("@ss.hasPermission('dm:transport-plan:create')")
    public CommonResult<Long> createTransportPlan(@Valid @RequestBody TransportPlanSaveReqVO createReqVO) {
        return success(transportPlanService.createTransportPlan(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新头程计划")
    @PreAuthorize("@ss.hasPermission('dm:transport-plan:update')")
    public CommonResult<Boolean> updateTransportPlan(@Valid @RequestBody TransportPlanSaveReqVO updateReqVO) {
        // 获取更新前的状态
        TransportPlanDO oldTransportPlan = transportPlanService.getTransportPlan(updateReqVO.getId());
        Integer oldStatus = oldTransportPlan.getTransportStatus();
        
        // 更新头程计划
        transportPlanService.updateTransportPlan(updateReqVO);
        
        // 状态有变化时，发送通知
        if (oldStatus != null && !oldStatus.equals(updateReqVO.getTransportStatus())) {
            sendTransportStatusNotify(oldTransportPlan, updateReqVO.getTransportStatus());
        }
        
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "作废头程计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:transport-plan:delete')")
    public CommonResult<Boolean> deleteTransportPlan(@RequestParam("id") Long id) {
        // 获取删除前的信息
        TransportPlanDO transportPlan = transportPlanService.getTransportPlan(id);
        Integer oldStatus = transportPlan.getTransportStatus();
        
        // 删除头程计划
        transportPlanService.deleteTransportPlan(id);
        
        // 发送状态变更通知（已作废状态为99）
        sendTransportStatusNotify(transportPlan, 99);
        
        return success(true);
    }
    
    /**
     * 发送头程计划状态变更通知
     * 
     * @param transportPlan 头程计划对象
     * @param newStatus 新状态
     */
    private void sendTransportStatusNotify(TransportPlanDO transportPlan, Integer newStatus) {
        try {
            Integer oldStatus = transportPlan.getTransportStatus();
            String transportCode = transportPlan.getCode();
            Long transportId = transportPlan.getId();
            
            // 1. 获取所有明细涉及的运营人员
            List<TransportPlanItemDO> transportPlanItems = transportPlanService.getTransportPlanItemListByPlanId(transportId);
            if (CollectionUtils.isEmpty(transportPlanItems)) {
                return;
            }
            
            // 2. 获取采购订单项信息
            List<Long> purchaseOrderItemIds = convertList(transportPlanItems, TransportPlanItemDO::getPurchaseOrderItemId);
            List<PurchaseOrderItemDO> purchaseOrderItemDOList = purchaseOrderService.batchPurchaseItemListByIds(purchaseOrderItemIds);
            
            // 3. 获取采购计划项信息
            List<String> planNumbers = convertList(purchaseOrderItemDOList, PurchaseOrderItemDO::getPlanNumber);
            List<PurchasePlanItemDO> purchasePlanItemList = purchasePlanService.batchPurchasePlanItemListByPlanNumbers(planNumbers);
            
            // 4. 提取所有运营人员ID
            Set<Long> operatorIds = new HashSet<>();
            for (PurchasePlanItemDO purchasePlanItem : purchasePlanItemList) {
                operatorIds.add(Long.valueOf(purchasePlanItem.getCreator()));
            }
            
            // 5. 获取状态名称（使用字典服务）
            String oldStatusName = oldStatus != null ? DictFrameworkUtils.parseDictDataLabel(DictTypeConstant.DM_TRANSPORT_STATUS, oldStatus) : "未知状态";
            String newStatusName = newStatus != null ? DictFrameworkUtils.parseDictDataLabel(DictTypeConstant.DM_TRANSPORT_STATUS, newStatus) : "未知状态";
            
            // 6. 发送通知给所有相关运营人员
            for (Long operatorId : operatorIds) {
                AdminUserRespDTO operator = adminUserApi.getUser(operatorId).getCheckedData();
                if (operator != null) {
                    // 构建通知参数
                    Map<String, Object> templateParams = new HashMap<>();
                    templateParams.put("userName", operator.getNickname());
                    templateParams.put("transportCode", transportCode);
                    templateParams.put("oldStatus", oldStatusName);
                    templateParams.put("newStatus", newStatusName);
                    templateParams.put("transportId", transportId.toString());
                    
                    // 发送站内信
                    NotifySendSingleToUserReqDTO notifyReqDTO = new NotifySendSingleToUserReqDTO();
                    notifyReqDTO.setUserId(operatorId);
                    notifyReqDTO.setTemplateCode("dm_transport_status_notify");
                    notifyReqDTO.setTemplateParams(templateParams);
                    notifyMessageSendApi.sendSingleMessageToAdmin(notifyReqDTO);
                }
            }
        } catch (Exception e) {
            // 通知发送异常不影响主流程
            e.printStackTrace();
        }
    }

    @GetMapping("/get")
    @Operation(summary = "获得头程计划")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:transport-plan:query')")
    public CommonResult<TransportPlanRespVO> getTransportPlan(@RequestParam("id") Long id) {
        TransportPlanDO transportPlan = transportPlanService.getTransportPlan(id);
        return success(BeanUtils.toBean(transportPlan, TransportPlanRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得头程计划分页")
    @PreAuthorize("@ss.hasPermission('dm:transport-plan:query')")
    public CommonResult<PageResult<TransportPlanRespVO>> getTransportPlanPage(@Valid TransportPlanPageReqVO pageReqVO) {
        PageResult<TransportPlanDO> pageResult = transportPlanService.getTransportPlanPage(pageReqVO);

        // 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(pageResult.getList(),
                purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator()))));


        return success(BeanUtils.toBean(pageResult, TransportPlanRespVO.class, planRespVO -> {
            MapUtils.findAndThen(userMap, Long.parseLong(planRespVO.getCreator()), user -> planRespVO.setCreatorName(user.getNickname()));
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出头程计划 Excel")
    @PreAuthorize("@ss.hasPermission('dm:transport-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTransportPlanExcel(@Valid TransportPlanPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TransportPlanDO> list = transportPlanService.getTransportPlanPage(pageReqVO).getList();

        // 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(list,
                purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator()))));
        // 导出 Excel
        ExcelUtils.write(response, "头程计划.xls", "数据", TransportPlanRespVO.class,
                BeanUtils.toBean(list, TransportPlanRespVO.class, planRespVO -> {
                    MapUtils.findAndThen(userMap, Long.parseLong(planRespVO.getCreator()), user -> planRespVO.setCreatorName(user.getNickname()));
                }));
    }

    // ==================== 子表（头程计划明细） ====================

    @Resource
    private PurchaseOrderService purchaseOrderService;
    @Resource
    private PurchasePlanService purchasePlanService;

    @GetMapping("/transport-plan-item/list-by-plan-id")
    @Operation(summary = "获得头程计划明细列表")
    @Parameter(name = "planId", description = "头程计划ID")
    @PreAuthorize("@ss.hasPermission('dm:transport-plan:query')")
    public CommonResult<List<TransportPlanItemRespVO>> getTransportPlanItemListByPlanId(@RequestParam("planId") Long planId) throws MalformedURLException {
        List<TransportPlanItemDO> transportPlanItemList = transportPlanService.getTransportPlanItemListByPlanId(planId);

        List<Long> purchaseOrderItemIds = convertList(transportPlanItemList, TransportPlanItemDO::getPurchaseOrderItemId);
        List<PurchaseOrderItemDO> purchaseOrderItemDOList = purchaseOrderService.batchPurchaseItemListByIds(purchaseOrderItemIds);

        List<String> planNumbers = convertList(purchaseOrderItemDOList, PurchaseOrderItemDO::getPlanNumber);
        List<PurchasePlanItemDO> purchasePlanItemList = purchasePlanService.batchPurchasePlanItemListByPlanNumbers(planNumbers);

        List<Long> orderIds = convertList(purchaseOrderItemDOList, PurchaseOrderItemDO::getOrderId);
        List<PurchaseOrderDO> purchaseOrderList = purchaseOrderService.getPurchaseOrderList(orderIds);

        List<Long> operateUserIds = convertListByFlatMap(purchasePlanItemList, purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator())));
        List<Long> productIds = convertList(transportPlanItemList, TransportPlanItemDO::getProductId);

        Map<String, PurchasePlanItemDO> planItemDOMap = convertMap(purchasePlanItemList, PurchasePlanItemDO::getPlanNumber);
        Map<Long, PurchaseOrderItemDO> purchaseOrderItemDOMap = convertMap(purchaseOrderItemDOList, PurchaseOrderItemDO::getId);
        Map<Long, ProductSimpleInfoVO> productSimpleInfoVOMap = productInfoService.batchQueryProductSimpleInfo(productIds);
        Map<Long, PurchaseOrderDO> purchaseOrderDOMap = convertMap(purchaseOrderList, PurchaseOrderDO::getId);


        // 管理员信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(operateUserIds);

        List<TransportPlanItemRespVO> transportPlanItemRespVOList = new ArrayList<>();
        for (TransportPlanItemDO transportPlanItemDO : transportPlanItemList) {
            TransportPlanItemRespVO transportPlanRespVO = BeanUtils.toBean(transportPlanItemDO, TransportPlanItemRespVO.class);

            Long purchaseOrderItemId = transportPlanItemDO.getPurchaseOrderItemId();
            PurchaseOrderItemDO purchaseOrderItemDO = purchaseOrderItemDOMap.get(purchaseOrderItemId);
            if (purchaseOrderItemDO != null) {
                String planNumber = purchaseOrderItemDO.getPlanNumber();
                PurchasePlanItemDO purchasePlanItemDO = planItemDOMap.get(planNumber);
                AdminUserRespDTO user = userMap.get(Long.valueOf(purchasePlanItemDO.getCreator()));

                if (purchaseOrderItemDO.getTaxPrice() != null) {
                    transportPlanRespVO.setTotalTaxAmount(purchaseOrderItemDO.getTaxPrice().multiply(new BigDecimal(transportPlanItemDO.getQuantity())));
                }
                if (purchaseOrderItemDO.getPrice() != null) {
                    transportPlanRespVO.setTotalAmount(purchaseOrderItemDO.getPrice().multiply(new BigDecimal(transportPlanItemDO.getQuantity())));
                }
                transportPlanRespVO.setOperatorName(user.getNickname());
                transportPlanRespVO.setPlanNumber(purchaseOrderItemDO.getPlanNumber());

                PurchaseOrderDO purchaseOrderDO = purchaseOrderDOMap.get(purchaseOrderItemDO.getOrderId());
                transportPlanRespVO.setPoNumber(purchaseOrderDO.getOrderNo());
            }

            ProductSimpleInfoVO productSimpleInfoVO = productSimpleInfoVOMap.get(transportPlanItemDO.getProductId());
            if (productSimpleInfoVO != null) {
                transportPlanRespVO.setProductSimpleInfo(productSimpleInfoVO);
                transportPlanRespVO.setProductImage(productSimpleInfoVO.getImage());
                transportPlanRespVO.setProductSkuId(productSimpleInfoVO.getSkuId());
                transportPlanRespVO.setProductSkuName(productSimpleInfoVO.getSkuName());
            }


            transportPlanItemRespVOList.add(transportPlanRespVO);
        }

        transportPlanItemRespVOList.sort(Comparator.comparing(TransportPlanItemRespVO::getProductId));
        return success(transportPlanItemRespVOList);
    }

    @GetMapping("/export-item-excel")
    @Operation(summary = "导出头程计划 Excel")
    @PreAuthorize("@ss.hasPermission('dm:transport-plan:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportTransportPlanItemExcel(@RequestParam("planId") Long planId,
                                             HttpServletResponse response) throws IOException {

        CommonResult<List<TransportPlanItemRespVO>> result = getTransportPlanItemListByPlanId(planId);
        List<TransportPlanItemRespVO> itemRespVOList = result.getData();

        // 导出 Excel
        ExcelUtils.write(response, "头程计划明细.xlsx", "数据", TransportPlanItemRespVO.class, itemRespVOList);
    }

    @PostMapping("/detail/list")
    @Operation(summary = "获得头程计划明细列表")
    @PreAuthorize("@ss.hasPermission('dm:transport-plan:query')")
    public CommonResult<List<TransportPlanDetailRespVO>> getTransportPlanDetailList(@RequestBody TransportPlanDetailReqVO reqVO) {
        List<TransportPlanDetailDTO> transportPlanDetailList = transportPlanService.getTransportPlanDetailList(reqVO);
        if (CollectionUtils.isEmpty(transportPlanDetailList)) {
            return success(Collections.emptyList());
        }

        // 1. 获取采购订单项信息
        List<Long> purchaseOrderItemIds = convertList(transportPlanDetailList, TransportPlanDetailDTO::getPurchaseOrderItemId);
        List<PurchaseOrderItemDO> purchaseOrderItemDOList = purchaseOrderService.batchPurchaseItemListByIds(purchaseOrderItemIds);
        Map<Long, PurchaseOrderItemDO> purchaseOrderItemDOMap = convertMap(purchaseOrderItemDOList, PurchaseOrderItemDO::getId);

        // 2. 获取采购计划项信息
        List<String> planNumbers = convertList(purchaseOrderItemDOList, PurchaseOrderItemDO::getPlanNumber);
        List<PurchasePlanItemDO> purchasePlanItemList = purchasePlanService.batchPurchasePlanItemListByPlanNumbers(planNumbers);
        Map<String, PurchasePlanItemDO> planItemDOMap = convertMap(purchasePlanItemList, PurchasePlanItemDO::getPlanNumber);

        // 3. 获取采购订单信息
        List<Long> orderIds = convertList(purchaseOrderItemDOList, PurchaseOrderItemDO::getOrderId);
        List<PurchaseOrderDO> purchaseOrderList = purchaseOrderService.getPurchaseOrderList(orderIds);
        Map<Long, PurchaseOrderDO> purchaseOrderDOMap = convertMap(purchaseOrderList, PurchaseOrderDO::getId);

        // 4. 获取产品信息
        List<Long> productIds = convertList(transportPlanDetailList, TransportPlanDetailDTO::getProductId);
        Map<Long, ProductSimpleInfoVO> productSimpleInfoVOMap = productInfoService.batchQueryProductSimpleInfo(productIds);

        // 5. 获取运营人员信息
        List<Long> operateUserIds = convertListByFlatMap(purchasePlanItemList,
                purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator())));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(operateUserIds);

        // 6. 组装数据
        List<TransportPlanDetailRespVO> transportPlanDetailRespVOList = new ArrayList<>();
        for (TransportPlanDetailDTO transportPlanDetailDTO : transportPlanDetailList) {
            TransportPlanDetailRespVO detailRespVO = BeanUtils.toBean(transportPlanDetailDTO, TransportPlanDetailRespVO.class);

            // 6.1 设置采购相关信息
            Long purchaseOrderItemId = transportPlanDetailDTO.getPurchaseOrderItemId();
            PurchaseOrderItemDO purchaseOrderItemDO = purchaseOrderItemDOMap.get(purchaseOrderItemId);
            if (purchaseOrderItemDO != null) {
                String planNumber = purchaseOrderItemDO.getPlanNumber();
                PurchasePlanItemDO purchasePlanItemDO = planItemDOMap.get(planNumber);
                AdminUserRespDTO user = userMap.get(Long.valueOf(purchasePlanItemDO.getCreator()));

                if (purchaseOrderItemDO.getTaxPrice() != null) {
                    detailRespVO.setTotalTaxAmount(purchaseOrderItemDO.getTaxPrice()
                            .multiply(new BigDecimal(transportPlanDetailDTO.getQuantity())));
                }
                if (purchaseOrderItemDO.getPrice() != null) {
                    detailRespVO.setTotalAmount(purchaseOrderItemDO.getPrice()
                            .multiply(new BigDecimal(transportPlanDetailDTO.getQuantity())));
                }
                detailRespVO.setOperatorName(user.getNickname());
                detailRespVO.setPlanNumber(purchaseOrderItemDO.getPlanNumber());

                PurchaseOrderDO purchaseOrderDO = purchaseOrderDOMap.get(purchaseOrderItemDO.getOrderId());
                detailRespVO.setPoNumber(purchaseOrderDO.getOrderNo());
            }

            // 6.2 设置产品信息
            ProductSimpleInfoVO productSimpleInfoVO = productSimpleInfoVOMap.get(transportPlanDetailDTO.getProductId());
            if (productSimpleInfoVO != null) {
                detailRespVO.setProductSimpleInfo(productSimpleInfoVO);
                detailRespVO.setProductImage(productSimpleInfoVO.getImage());
                detailRespVO.setProductSkuId(productSimpleInfoVO.getSkuId());
                detailRespVO.setProductSkuName(productSimpleInfoVO.getSkuName());
            }

            transportPlanDetailRespVOList.add(detailRespVO);
        }

        transportPlanDetailRespVOList.sort(Comparator.comparing(TransportPlanDetailRespVO::getProductId));
        return success(transportPlanDetailRespVOList);
    }

}