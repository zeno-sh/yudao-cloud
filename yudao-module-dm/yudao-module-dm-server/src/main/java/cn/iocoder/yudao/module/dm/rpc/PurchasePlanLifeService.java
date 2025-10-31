package cn.iocoder.yudao.module.dm.rpc;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmTaskDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog.PurchaseOrderArrivedLogDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanItemDO;
import cn.iocoder.yudao.module.dm.enums.PurchaseLifeStatusEnum;
import cn.iocoder.yudao.module.dm.enums.PurchaseStepStatusEnum;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.purchase.PurchasePlanService;
import cn.iocoder.yudao.module.dm.service.purchase.log.PurchaseOrderArrivedLogService;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseOrderService;
import cn.iocoder.yudao.module.dm.service.purchase.plan.PurchasePlanItemService;
import cn.iocoder.yudao.module.dm.service.transport.TransportPlanService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

/**
 * @author: Zeno
 * @createTime: 2024/06/13 10:54
 */
@RestController
public class PurchasePlanLifeService {

    @Resource
    private PurchasePlanItemService purchasePlanItemService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PurchaseOrderArrivedLogService purchaseOrderArrivedLogService;
    @Resource
    private PurchaseOrderService purchaseOrderService;
    @Resource
    private TransportPlanService transportPlanService;
    @Resource
    private BpmProcessInstanceApi bpmProcessInstanceApi;
    @Resource
    private PurchasePlanService purchasePlanService;
    @Resource
    private ProductInfoService productInfoService;

    public List<PurchasePlanLifeRespVO> queryPurchasePlanLife(PurchasePlanLifeReqVO reqVO) {
        List<PurchasePlanItemDO> purchasePlanItemDOList = getPurchasePlanItems(reqVO);

        if (CollectionUtils.isEmpty(purchasePlanItemDOList)) {
            return Collections.emptyList();
        }

        List<String> planNumbers = convertList(purchasePlanItemDOList, PurchasePlanItemDO::getPlanNumber);

        List<PurchaseOrderItemDO> purchaseOrderItemDOList = purchaseOrderService.batchPurchaseItemListByPlanNumbers(planNumbers);

        if (CollectionUtils.isEmpty(purchaseOrderItemDOList)) {
            return buildPurchasePlanLifeRespVO(purchasePlanItemDOList, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        List<Long> orderItemIds = purchaseOrderItemDOList.stream()
                .map(PurchaseOrderItemDO::getId)
                .collect(Collectors.toList());

        List<PurchaseOrderArrivedLogDO> arrivedLogDOList = purchaseOrderArrivedLogService.batchPurchaseOrderArrivedLogByPurchaseItemIds(orderItemIds);
        List<TransportPlanItemDO> transportPlanItemDOList = transportPlanService.batchTransportPlanItemListByPurchaseOrderItemIds(orderItemIds);

        List<PurchasePlanLifeRespVO> purchasePlanLifeRespVOList = buildPurchasePlanLifeRespVO(purchasePlanItemDOList, purchaseOrderItemDOList, arrivedLogDOList, transportPlanItemDOList);
        if (StringUtils.isBlank(reqVO.getStatus())) {
            return purchasePlanLifeRespVOList;
        }
        return filterByStatus(purchasePlanLifeRespVOList, reqVO.getStatus());
    }

    private List<PurchasePlanLifeRespVO> filterByStatus(List<PurchasePlanLifeRespVO> originalList, String status) {
        // 处理后的计划状态列表
        List<PurchasePlanLifeRespVO> filteredPlans = new ArrayList<>();

        // 根据不同的状态定义对应的匹配逻辑
        for (PurchasePlanLifeRespVO lifeRespVO : originalList) {
            List<PurchasePlanItemProcessVO> process = lifeRespVO.getSteps().getProcess();

            boolean shouldAdd = false;

            switch (PurchaseLifeStatusEnum.valueOfStatus(status)) {
                case NOT_ARRIVED:
                    shouldAdd = process.stream()
                            .anyMatch(processVO -> "到货".equals(processVO.getStepTitle()) &&
                                    PurchaseStepStatusEnum.WAIT.getStatus().equals(processVO.getStatus()));
                    break;
                case UNSHIPPED:
                    shouldAdd = process.stream()
                            .anyMatch(processVO -> "发货".equals(processVO.getStepTitle()) &&
                                    PurchaseStepStatusEnum.WAIT.getStatus().equals(processVO.getStatus()) ||
                                    PurchaseStepStatusEnum.PROCESS.getStatus().equals(processVO.getStatus()));

                    break;
                case IN_TRANSIT:
                    shouldAdd = process.stream()
                            .anyMatch(processVO -> "发货".equals(processVO.getStepTitle()) &&
                                    PurchaseStepStatusEnum.SUCCESS.getStatus().equals(processVO.getStatus())) &&
                            process.stream()
                                    .anyMatch(processVO -> "到港".equals(processVO.getStepTitle()) &&
                                            PurchaseStepStatusEnum.WAIT.getStatus().equals(processVO.getStatus()));
                    break;
                case COMPLETED:
                    shouldAdd = process.stream()
                            .anyMatch(processVO -> "到门".equals(processVO.getStepTitle()) &&
                                    PurchaseStepStatusEnum.SUCCESS.getStatus().equals(processVO.getStatus()));
                    break;
                default:
                    break;
            }

            if (shouldAdd) {
                filteredPlans.add(lifeRespVO);
            }
        }

        return filteredPlans;
    }


    private List<PurchasePlanItemDO> getPurchasePlanItems(PurchasePlanLifeReqVO reqVO) {
        String sku = reqVO.getSku();
        String planNumber = reqVO.getPlanNumber();
        String orderNo = reqVO.getOrderNo();
        List<Long> appointUserIds = buildUserIds(reqVO.getAppointUserIds());
        String planBatchNumber = reqVO.getPlanBatchNumber();

        if (StringUtils.isNotBlank(reqVO.getSpu())) {
            List<ProductInfoDO> productInfoDOList = productInfoService.queryProductInfoListBySpuId(reqVO.getSpu());
            if (CollectionUtils.isNotEmpty(productInfoDOList)) {
                List<Long> productIds = convertList(productInfoDOList, ProductInfoDO::getId);
                return purchasePlanItemService.batchPurchasePlanItem(productIds, null, appointUserIds);
            }
        }

        if (StringUtils.isNotBlank(sku)) {
            List<ProductInfoDO> productInfoDOList = productInfoService.queryByKeyword(sku);
            if (CollectionUtils.isNotEmpty(productInfoDOList)) {
                List<Long> productIds = convertList(productInfoDOList, ProductInfoDO::getId);
                return purchasePlanItemService.batchPurchasePlanItem(productIds, null, appointUserIds);
            }
        }

        if (StringUtils.isNotBlank(planNumber)) {
            return purchasePlanItemService.batchPurchasePlanItem(null, Lists.newArrayList(planNumber), appointUserIds);
        }

        if (StringUtils.isNotBlank(orderNo)) {
            List<PurchaseOrderDO> purchaseOrderDOList = purchaseOrderService.batchPurchaseOrderList(Lists.newArrayList(orderNo));
            if (CollectionUtils.isNotEmpty(purchaseOrderDOList)) {
                List<Long> orderIds = convertList(purchaseOrderDOList, PurchaseOrderDO::getId);
                List<PurchaseOrderItemDO> purchaseOrderItemDOList = purchaseOrderService.batchPurchaseOrderItemListByOrderIds(orderIds);
                List<String> planNumbers = convertList(purchaseOrderItemDOList, PurchaseOrderItemDO::getPlanNumber);
                return purchasePlanItemService.batchPurchasePlanItem(null, planNumbers, appointUserIds);
            }
        }

        if (StringUtils.isNotBlank(planBatchNumber)) {
            List<PurchasePlanDO> purchasePlanDOList = purchasePlanService.batchQueryPurchasePlanByNumber(Lists.newArrayList(planBatchNumber));
            if (CollectionUtils.isNotEmpty(purchasePlanDOList)) {
                return purchasePlanService.batchPurchasePlanItemListByPlanIds(convertList(purchasePlanDOList, PurchasePlanDO::getId));
            }
        }

        return null;
    }


    private List<Long> buildUserIds(List<Long> appointUserIds) {
//        if (CollectionUtils.isEmpty(appointUserIds)) {
//            List<AdminUserRespDTO> userListByDataPermissions = adminUserApi.getUserListByDataPermissions();
//            return convertList(userListByDataPermissions, AdminUserRespDTO::getId);
//        } else {
//            return appointUserIds;
//        }
        return appointUserIds;
    }


    private List<PurchasePlanLifeRespVO> buildPurchasePlanLifeRespVO(List<PurchasePlanItemDO> purchasePlanItemDOList,
                                                                     List<PurchaseOrderItemDO> purchaseOrderItemDOList,
                                                                     List<PurchaseOrderArrivedLogDO> arrivedLogDOList,
                                                                     List<TransportPlanItemDO> transportPlanItemDOList) {
        List<PurchasePlanLifeRespVO> purchasePlanLifeRespVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(purchasePlanItemDOList)) {
            return purchasePlanLifeRespVOList;
        }

        Map<Long, AdminUserRespDTO> purchasePlanOperatorMap = adminUserApi.getUserMap(
                purchasePlanItemDOList.stream()
                        .map(item -> NumberUtils.parseLong(item.getCreator()))
                        .collect(Collectors.toList())
        );

        Map<String, List<PurchaseOrderItemDO>> purchaseOrderItemDOMap = convertMultiMap(purchaseOrderItemDOList, PurchaseOrderItemDO::getPlanNumber);
        Map<Long, List<PurchaseOrderArrivedLogDO>> purchaseOrderArrivedLogDOMap = convertMultiMap(arrivedLogDOList, PurchaseOrderArrivedLogDO::getPurchaseOrderItemId);
        Map<Long, List<TransportPlanItemDO>> transportPlanItemDOMap = convertMultiMap(transportPlanItemDOList, TransportPlanItemDO::getPurchaseOrderItemId);

        List<Long> planIds = convertList(purchasePlanItemDOList, PurchasePlanItemDO::getPlanId);
        Map<Long, PurchasePlanDO> purchasePlanDOMap = convertMap(purchasePlanService.batchQueryPurchasePlan(planIds), PurchasePlanDO::getId);

        List<Long> orderIds = convertList(purchaseOrderItemDOList, PurchaseOrderItemDO::getOrderId);
        Map<Long, PurchaseOrderDO> purchaseOrderDOMap = convertMap(purchaseOrderService.getPurchaseOrderList(orderIds), PurchaseOrderDO::getId);

        for (PurchasePlanItemDO purchasePlanItemDO : purchasePlanItemDOList) {
            String planNumber = purchasePlanItemDO.getPlanNumber();
            PurchasePlanDO purchasePlanDO = purchasePlanDOMap.get(purchasePlanItemDO.getPlanId());

            if (purchasePlanDO == null) {
                continue;
            }

            List<PurchaseOrderItemDO> existOrderItemList = purchaseOrderItemDOMap.get(planNumber);
            if (CollectionUtils.isEmpty(existOrderItemList)) {
                purchasePlanLifeRespVOList.add(buildLifeRespVO(purchasePlanItemDO, null, null, null, purchasePlanOperatorMap, purchaseOrderDOMap));
                continue;
            }

            for (PurchaseOrderItemDO purchaseOrderItemDO : existOrderItemList) {
                List<TransportPlanItemDO> transportPlanItems = transportPlanItemDOMap.getOrDefault(purchaseOrderItemDO.getId(), Collections.emptyList());

                if (CollectionUtils.isEmpty(transportPlanItems)) {
                    PurchasePlanLifeRespVO purchasePlanLifeRespVO = buildLifeRespVO(purchasePlanItemDO, purchaseOrderItemDO, purchaseOrderArrivedLogDOMap, null, purchasePlanOperatorMap, purchaseOrderDOMap);
                    populateCommonFields(purchasePlanLifeRespVO, purchaseOrderDOMap, purchasePlanDO, purchaseOrderItemDO);
                    purchasePlanLifeRespVOList.add(purchasePlanLifeRespVO);
                } else {
                    for (TransportPlanItemDO transportPlanItemDO : transportPlanItems) {
                        PurchasePlanLifeRespVO purchasePlanLifeRespVO = buildLifeRespVO(purchasePlanItemDO, purchaseOrderItemDO, purchaseOrderArrivedLogDOMap, transportPlanItemDO, purchasePlanOperatorMap, purchaseOrderDOMap);
                        populateCommonFields(purchasePlanLifeRespVO, purchaseOrderDOMap, purchasePlanDO, purchaseOrderItemDO);
                        purchasePlanLifeRespVOList.add(purchasePlanLifeRespVO);
                    }
                }
            }
        }
        return purchasePlanLifeRespVOList;
    }

    private void populateCommonFields(PurchasePlanLifeRespVO purchasePlanLifeRespVO, Map<Long, PurchaseOrderDO> purchaseOrderDOMap, PurchasePlanDO purchasePlanDO, PurchaseOrderItemDO purchaseOrderItemDO) {
        PurchaseOrderDO purchaseOrderDO = purchaseOrderDOMap.get(purchaseOrderItemDO.getOrderId());
        if (purchaseOrderDO != null) {
            purchasePlanLifeRespVO.setPurchaseOrderNumber(purchaseOrderDO.getOrderNo());
        }
        purchasePlanLifeRespVO.setBatchNumber(purchasePlanDO.getBatchNumber());
    }


    private PurchasePlanLifeRespVO buildLifeRespVO(PurchasePlanItemDO purchasePlanItemDO,
                                                   PurchaseOrderItemDO purchaseOrderItemDO,
                                                   Map<Long, List<PurchaseOrderArrivedLogDO>> purchaseOrderArrivedLogDOMap,
                                                   TransportPlanItemDO transportPlanItemDO,
                                                   Map<Long, AdminUserRespDTO> purchasePlanOperatorMap,
                                                   Map<Long, PurchaseOrderDO> purchaseOrderDOMap) {

        PurchasePlanLifeRespVO lifeRespVO = new PurchasePlanLifeRespVO();
        lifeRespVO.setPlanNumber(purchasePlanItemDO.getPlanNumber());
        lifeRespVO.setPlanId(purchasePlanItemDO.getId());
        lifeRespVO.setProductId(purchasePlanItemDO.getProductId());
        lifeRespVO.setPurchaseQuantity(purchasePlanItemDO.getQuantity());
        lifeRespVO.setOperator(purchasePlanOperatorMap.get(NumberUtils.parseLong(purchasePlanItemDO.getCreator())).getNickname());

        PurchasePlanItemStepsVO stepsVO = new PurchasePlanItemStepsVO();
        stepsVO.setCurrentStep(0);
        stepsVO.setProcess(new LinkedList<>());

        buildPlanStep(purchasePlanItemDO, stepsVO);
        buildPurchaseOrderStep(purchaseOrderDOMap, purchaseOrderItemDO, stepsVO);
        if (null == purchaseOrderItemDO) {
            buildArrivedStep(null, stepsVO);
        } else {
            lifeRespVO.setOrderItemId(purchaseOrderItemDO.getId());
            buildArrivedStep(purchaseOrderArrivedLogDOMap.get(purchaseOrderItemDO.getId()), stepsVO);
        }
        buildTransportStep(transportPlanItemDO, stepsVO, lifeRespVO);

        lifeRespVO.setSteps(stepsVO);
        return lifeRespVO;
    }

    private PurchasePlanItemProcessItemVO createProcessItemVO(String content, String nickname, String datetime) {
        PurchasePlanItemProcessItemVO itemVO = new PurchasePlanItemProcessItemVO();
        itemVO.setContent(content);
        itemVO.setName(nickname);
        itemVO.setDateTime(datetime);
        return itemVO;
    }

    private void buildPlanStep(PurchasePlanItemDO purchasePlanItemDO, PurchasePlanItemStepsVO stepsVO) {
        List<PurchasePlanItemProcessVO> process = stepsVO.getProcess();

        PurchasePlanItemProcessVO processVO = new PurchasePlanItemProcessVO();
        processVO.setStepTitle("计划");
        processVO.setStatus(PurchaseStepStatusEnum.WAIT.getStatus());
        List<PurchasePlanItemProcessItemVO> items = new LinkedList<>();
        if (null != purchasePlanItemDO) {
            stepsVO.setCurrentStep(1);

            String nickname = adminUserApi.getUser(NumberUtils.parseLong(purchasePlanItemDO.getCreator())).getCheckedData().getNickname();
            items.add(createProcessItemVO("创建", nickname,
                    DateUtil.format(purchasePlanItemDO.getCreateTime(), "MM-dd HH:mm")));
            processVO.setStatus(PurchaseStepStatusEnum.PROCESS.getStatus());

            if (0 != purchasePlanItemDO.getAuditStatus()) {
                items.add(createProcessItemVO("已提交", nickname,
                        DateUtil.format(purchasePlanItemDO.getUpdateTime(), "MM-dd HH:mm")));
            }
            if (BpmTaskStatusEnum.APPROVE.getStatus().equals(purchasePlanItemDO.getAuditStatus())) {
                items.add(createProcessItemVO("审核通过", null, null));
                processVO.setStatus(PurchaseStepStatusEnum.SUCCESS.getStatus());
            } else if (BpmTaskStatusEnum.RUNNING.getStatus().equals(purchasePlanItemDO.getAuditStatus())) {
                items.add(createProcessItemVO("审批中", null, null));
            } else if (BpmTaskStatusEnum.REJECT.getStatus().equals(purchasePlanItemDO.getAuditStatus())) {

                processVO.setStatus(PurchaseStepStatusEnum.ERROR.getStatus());
                // TODO: 添加驳回原因
                PurchasePlanDO purchasePlan = purchasePlanService.getPurchasePlan(purchasePlanItemDO.getPlanId());
                CommonResult<List<BpmTaskDTO>> result = bpmProcessInstanceApi.getProcessTaskInfo(purchasePlan.getProcessInstanceId());
                List<BpmTaskDTO> processTaskInfoList = result.getCheckedData();
                processTaskInfoList.stream().filter(bpmTaskDTO -> BpmTaskStatusEnum.REJECT.getStatus().equals(bpmTaskDTO.getStatus()))
                        .forEach(bpmTaskDTO -> {
                            items.add(createProcessItemVO(String.format("审核驳回：%s", bpmTaskDTO.getReason()),
                                    bpmTaskDTO.getAssigneeUser().getNickname(), DateUtil.format(bpmTaskDTO.getEndTime(), "MM-dd HH:mm")));
                        });
            }

            if (purchasePlanItemDO.getStatus() == 30) {
                processVO.setStatus(PurchaseStepStatusEnum.ERROR.getStatus());
                items.add(createProcessItemVO("已作废", nickname,
                        DateUtil.format(purchasePlanItemDO.getUpdateTime(), "MM-dd HH:mm")));
            }
        }
        processVO.setItems(items);
        process.add(processVO);
    }

    private void buildPurchaseOrderStep(Map<Long, PurchaseOrderDO> purchaseOrderDOMap, PurchaseOrderItemDO purchaseOrderItem, PurchasePlanItemStepsVO stepsVO) {
        List<PurchasePlanItemProcessVO> process = stepsVO.getProcess();
        PurchasePlanItemProcessVO processVO = new PurchasePlanItemProcessVO();
        processVO.setStepTitle("采购");
        processVO.setStatus(PurchaseStepStatusEnum.WAIT.getStatus());

        List<PurchasePlanItemProcessItemVO> items = new LinkedList<>();
        if (purchaseOrderItem != null) {
            stepsVO.setCurrentStep(2);
            String nickname = adminUserApi.getUser(NumberUtils.parseLong(purchaseOrderItem.getCreator())).getCheckedData().getNickname();
            PurchaseOrderDO purchaseOrderDO = purchaseOrderDOMap.get(purchaseOrderItem.getOrderId());
            Integer status = purchaseOrderDO.getStatus();
            if (status == 99) {
                items.add(createProcessItemVO("已作废", nickname,
                        DateUtil.format(purchaseOrderItem.getUpdateTime(), "MM-dd HH:mm")));
                processVO.setStatus(PurchaseStepStatusEnum.ERROR.getStatus());
            } else if (status == 30) {
                items.add(createProcessItemVO("下单", nickname,
                        DateUtil.format(purchaseOrderItem.getCreateTime(), "MM-dd HH:mm")));
                processVO.setStatus(PurchaseStepStatusEnum.SUCCESS.getStatus());
            } else if (status == 10 || status == 0) {
                items.add(createProcessItemVO("待下单", nickname,
                        DateUtil.format(purchaseOrderItem.getCreateTime(), "MM-dd HH:mm")));
                processVO.setStatus(PurchaseStepStatusEnum.PROCESS.getStatus());
            }

            if (purchaseOrderItem.getStatus() == 20) {
                items.add(createProcessItemVO("待到货", null,
                        DateUtil.format(purchaseOrderItem.getUpdateTime(), "MM-dd HH:mm")));

            }
        }
        processVO.setItems(items);
        process.add(processVO);
    }

    private void buildArrivedStep(List<PurchaseOrderArrivedLogDO> purchaseOrderArrivedLogDOList, PurchasePlanItemStepsVO stepsVO) {
        List<PurchasePlanItemProcessVO> process = stepsVO.getProcess();
        PurchasePlanItemProcessVO processVO = new PurchasePlanItemProcessVO();
        processVO.setStepTitle("到货");
        processVO.setStatus(PurchaseStepStatusEnum.WAIT.getStatus());

        List<PurchasePlanItemProcessItemVO> items = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(purchaseOrderArrivedLogDOList)) {
            stepsVO.setCurrentStep(3);
            for (PurchaseOrderArrivedLogDO arrivedLogDO : purchaseOrderArrivedLogDOList) {
                String nickname = adminUserApi.getUser(NumberUtils.parseLong(arrivedLogDO.getCreator())).getCheckedData().getNickname();
                items.add(createProcessItemVO(String.format("到货数量：%d", arrivedLogDO.getArrivedQuantity()), nickname,
                        DateUtil.format(arrivedLogDO.getCreateTime(), "MM-dd HH:mm")));
                processVO.setStatus(PurchaseStepStatusEnum.SUCCESS.getStatus());
            }

        }
        processVO.setItems(items);
        process.add(processVO);
    }

    private void buildTransportStep(TransportPlanItemDO transportPlanItemDO, PurchasePlanItemStepsVO stepsVO, PurchasePlanLifeRespVO lifeRespVO) {
        List<PurchasePlanItemProcessVO> process = stepsVO.getProcess();

        // 发货步骤
        PurchasePlanItemProcessVO transportProcessVO = new PurchasePlanItemProcessVO();
        transportProcessVO.setStepTitle("发货");
        transportProcessVO.setStatus(PurchaseStepStatusEnum.WAIT.getStatus());

        // 到港步骤
        PurchasePlanItemProcessVO arrivedPortProcessVO = new PurchasePlanItemProcessVO();
        arrivedPortProcessVO.setStepTitle("到港");
        arrivedPortProcessVO.setStatus(PurchaseStepStatusEnum.WAIT.getStatus());


        // 到门步骤
        PurchasePlanItemProcessVO arrivedDoorProcessVO = new PurchasePlanItemProcessVO();
        arrivedDoorProcessVO.setStepTitle("到门");
        arrivedDoorProcessVO.setStatus(PurchaseStepStatusEnum.WAIT.getStatus());

        List<PurchasePlanItemProcessItemVO> transportItems = new LinkedList<>();
        List<PurchasePlanItemProcessItemVO> arrivedPortItems = new LinkedList<>();
        List<PurchasePlanItemProcessItemVO> arrivedDoorItems = new LinkedList<>();

        if (null != transportPlanItemDO) {
            Long planId = transportPlanItemDO.getPlanId();
            TransportPlanDO transportPlan = transportPlanService.getTransportPlan(planId);
            if (null != transportPlan) {
                lifeRespVO.setTransportPlanNumber(transportPlan.getCode());

                String creatorNickname = adminUserApi.getUser(NumberUtils.parseLong(transportPlanItemDO.getCreator())).getCheckedData().getNickname();
                String updaterNickname = adminUserApi.getUser(NumberUtils.parseLong(transportPlanItemDO.getUpdater())).getCheckedData().getNickname();

                // 处理发货状态
                stepsVO.setCurrentStep(4);
                if (transportPlan.getTransportStatus() == 0) {
                    transportItems.add(createProcessItemVO(String.format("询价中 数量：%d", transportPlanItemDO.getQuantity()), creatorNickname,
                            DateUtil.format(transportPlanItemDO.getCreateTime(), "MM-dd HH:mm")));
                    transportProcessVO.setStatus(PurchaseStepStatusEnum.PROCESS.getStatus());
                } else {
                    transportItems.add(createProcessItemVO(String.format("运输中 数量：%d", transportPlanItemDO.getQuantity()), creatorNickname,
                            DateUtil.format(transportPlanItemDO.getUpdateTime(), "MM-dd HH:mm")));
                    transportProcessVO.setStatus(PurchaseStepStatusEnum.SUCCESS.getStatus());
                }

                // 处理抵达状态
                if (transportPlan.getTransportStatus().equals(30)) {
                    stepsVO.setCurrentStep(5);
                    arrivedPortItems.add(createProcessItemVO("已完成", updaterNickname,
                            DateUtil.format(transportPlanItemDO.getUpdateTime(), "MM-dd HH:mm")));
                    arrivedPortProcessVO.setStatus(PurchaseStepStatusEnum.SUCCESS.getStatus());
                } else if (transportPlan.getTransportStatus().equals(40)) {
                    stepsVO.setCurrentStep(6);
                    //需要填充到港的信息
                    arrivedPortProcessVO.setStatus(PurchaseStepStatusEnum.SUCCESS.getStatus());

                    arrivedDoorItems.add(createProcessItemVO("已完成", updaterNickname,
                            DateUtil.format(transportPlanItemDO.getUpdateTime(), "MM-dd HH:mm")));
                    arrivedDoorProcessVO.setStatus(PurchaseStepStatusEnum.SUCCESS.getStatus());
                    lifeRespVO.setFinishedDate(transportPlan.getFinishedDate());
                }

                lifeRespVO.setTransportQuantity(transportPlanItemDO.getQuantity());
                lifeRespVO.setArrivalDate(transportPlan.getArrivalDate());
            }
        }

        transportProcessVO.setItems(transportItems);
        arrivedPortProcessVO.setItems(arrivedPortItems);
        arrivedDoorProcessVO.setItems(arrivedDoorItems);
        process.add(transportProcessVO);
        process.add(arrivedPortProcessVO);
        process.add(arrivedDoorProcessVO);
    }
}
