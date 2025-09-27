package cn.iocoder.yudao.module.dm.service.purchase.order;

import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo.PurchaseOrderItemVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog.PurchaseOrderArrivedLogDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanItemDO;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.purchase.PurchasePlanService;
import cn.iocoder.yudao.module.dm.service.transport.TransportPlanService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

/**
 * @author: Zeno
 * @createTime: 2024/07/16 21:49
 */
@Service
public class PurchaseOrderManagerService {

    @Resource
    private PurchaseOrderService purchaseOrderService;
    @Resource
    private PurchasePlanService purchasePlanService;
    @Resource
    private TransportPlanService transportPlanService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private ProductInfoService productInfoService;

//    public List<PurchaseOrderItemVO> buildPurchaseOrderItemVO(Long orderId, List<PurchaseOrderItemDO> purchaseOrderItemList) {
//
//        List<PurchaseOrderArrivedLogDO> arrivedLogList = purchaseOrderService.getPurchaseOrderArrivedLogListByPurchaseOrderId(orderId);
//        // 将List转换为Map<purchaseOrderItemId, 累计arrivedQuantity>
//        Map<Long, Integer> arrivedQuantityMap = arrivedLogList.stream()
//                .collect(Collectors.groupingBy(
//                        PurchaseOrderArrivedLogDO::getPurchaseOrderItemId,
//                        Collectors.summingInt(PurchaseOrderArrivedLogDO::getArrivedQuantity)
//                ));
//
//        // 采购计划的负责人
//        List<String> planNumbers = convertList(purchaseOrderItemList, PurchaseOrderItemDO::getPlanNumber);
//        List<PurchasePlanItemDO> purchasePlanItemList = purchasePlanService.batchPurchasePlanItemListByPlanNumbers(planNumbers);
//        List<Long> operateUserIds = convertListByFlatMap(purchasePlanItemList, purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator())));
//        List<Long> createUserIds = convertListByFlatMap(purchaseOrderItemList, purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator())));
//        createUserIds.addAll(operateUserIds);
//
//        // 管理员信息
//        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(createUserIds);
//
//        Map<String, PurchasePlanItemDO> planItemMap = convertMap(purchasePlanItemList, PurchasePlanItemDO::getPlanNumber);
//
//        List<PurchaseOrderItemVO> itemVOList = BeanUtils.toBean(purchaseOrderItemList, PurchaseOrderItemVO.class, itemVO -> {
//            MapUtils.findAndThen(userMap, Long.parseLong(itemVO.getCreator()), user -> itemVO.setCreatorName(user.getNickname()));
//            MapUtils.findAndThen(arrivedQuantityMap, itemVO.getId(), arrivedQuantity -> itemVO.setTotalArrivedQuantity(arrivedQuantity));
//            Integer totalArrivedQuantity = arrivedQuantityMap.get(itemVO.getId());
//            if (Objects.isNull(totalArrivedQuantity)) {
//                itemVO.setRemainingQuantity(itemVO.getPurchaseQuantity());
//                itemVO.setTotalArrivedQuantity(0);
//            } else {
//                itemVO.setRemainingQuantity(itemVO.getPurchaseQuantity() - totalArrivedQuantity);
//            }
//
//            PurchasePlanItemDO purchasePlanItemDO = planItemMap.get(itemVO.getPlanNumber());
//            MapUtils.findAndThen(userMap, Long.parseLong(purchasePlanItemDO.getCreator()), user -> itemVO.setOperateName(user.getNickname()));
//            itemVO.setExpectedArrivalDate(purchasePlanItemDO.getExpectedArrivalDate());
//        });
//
//        itemVOList.sort(Comparator.comparing(PurchaseOrderItemVO::getProductId));
//        return itemVOList;
//    }
//
//    public List<PurchaseOrderItemVO> buildPurchaseOrderItemVO(List<PurchaseOrderItemDO> purchaseOrderItemList) {
//
//        // 采购单号
//        List<Long> orderIds = convertList(purchaseOrderItemList, PurchaseOrderItemDO::getOrderId);
//        List<PurchaseOrderDO> purchaseOrderList = purchaseOrderService.getPurchaseOrderList(orderIds);
//        Map<Long, PurchaseOrderDO> orderMap = convertMap(purchaseOrderList, PurchaseOrderDO::getId);
//
//        List<PurchaseOrderArrivedLogDO> arrivedLogDOList = purchaseOrderService.batchPurchaseOrderArrivedLogListByPurchaseOrderIds(orderIds);
//        // 将List转换为Map<purchaseOrderItemId, 累计arrivedQuantity>
//        Map<Long, Integer> arrivedQuantityMap = arrivedLogDOList.stream()
//                .collect(Collectors.groupingBy(
//                        PurchaseOrderArrivedLogDO::getPurchaseOrderItemId,
//                        Collectors.summingInt(PurchaseOrderArrivedLogDO::getArrivedQuantity)
//                ));
//
//        List<Long> purchaseOrderItemIds = convertList(purchaseOrderItemList, PurchaseOrderItemDO::getId);
//        List<TransportPlanItemDO> transportPlanItemDOList = transportPlanService.batchTransportPlanItemListByPurchaseOrderItemIds(purchaseOrderItemIds);
//        Map<Long, Integer> transportQuantityMap = transportPlanItemDOList.stream()
//                .collect(Collectors.groupingBy(
//                        TransportPlanItemDO::getPurchaseOrderItemId,
//                        Collectors.summingInt(TransportPlanItemDO::getQuantity)
//                ));
//
//        // 采购计划的负责人
//        List<String> planNumbers = convertList(purchaseOrderItemList, PurchaseOrderItemDO::getPlanNumber);
//        List<PurchasePlanItemDO> purchasePlanItemList = purchasePlanService.batchPurchasePlanItemListByPlanNumbers(planNumbers);
//        List<Long> operateUserIds = convertListByFlatMap(purchasePlanItemList, purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator())));
//        List<Long> createUserIds = convertListByFlatMap(purchaseOrderItemList, purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator())));
//        createUserIds.addAll(operateUserIds);
//
//        // 管理员信息
//        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(createUserIds);
//        Map<String, PurchasePlanItemDO> planItemMap = convertMap(purchasePlanItemList, PurchasePlanItemDO::getPlanNumber);
//
//        return BeanUtils.toBean(purchaseOrderItemList, PurchaseOrderItemVO.class, itemVO -> {
//            MapUtils.findAndThen(userMap, Long.parseLong(itemVO.getCreator()), user -> itemVO.setCreatorName(user.getNickname()));
//            MapUtils.findAndThen(orderMap, itemVO.getOrderId(), orderDO -> itemVO.setOrderNo(orderDO.getOrderNo()));
//            MapUtils.findAndThen(arrivedQuantityMap, itemVO.getId(), arrivedQuantity -> itemVO.setTotalArrivedQuantity(arrivedQuantity));
//            Integer totalShippedQuantity = transportQuantityMap.get(itemVO.getId());
//            if (Objects.isNull(totalShippedQuantity)) {
//                itemVO.setTotalShippedQuantity(0);
//                itemVO.setRemainingQuantity(itemVO.getTotalArrivedQuantity());
//            } else {
//                itemVO.setTotalShippedQuantity(totalShippedQuantity);
//                itemVO.setRemainingQuantity(itemVO.getTotalArrivedQuantity() - totalShippedQuantity);
//            }
//
//            PurchasePlanItemDO purchasePlanItemDO = planItemMap.get(itemVO.getPlanNumber());
//            MapUtils.findAndThen(userMap, Long.parseLong(purchasePlanItemDO.getCreator()), user -> itemVO.setOperateName(user.getNickname()));
//        });
//    }

    public List<PurchaseOrderItemVO> buildPurchaseOrderItemVO(Long orderId, List<PurchaseOrderItemDO> purchaseOrderItemList) {
        Map<Long, Integer> arrivedQuantityMap = getArrivedQuantityMap(Collections.singletonList(orderId));
        List<Long> createUserIds = getCreateUserIds(purchaseOrderItemList);
        List<Long> productIds = convertList(purchaseOrderItemList, PurchaseOrderItemDO::getProductId);
        Map<Long, ProductSimpleInfoVO> productSimpleInfoVOMap = productInfoService.batchQueryProductSimpleInfo(productIds);

        Map<Long, ProductPurchaseDO> productPurchaseDOMap = productInfoService.batchProductPurchaseListByProductIds(ArrayUtils.toArray(productIds));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(createUserIds);
        Map<String, PurchasePlanItemDO> planItemMap = getPlanItemMap(purchaseOrderItemList);


        List<PurchaseOrderItemVO> itemVOList = BeanUtils.toBean(purchaseOrderItemList, PurchaseOrderItemVO.class, itemVO -> {
            setCommonFields(itemVO, userMap, arrivedQuantityMap, planItemMap);
            itemVO.setExpectedArrivalDate(planItemMap.get(itemVO.getPlanNumber()).getExpectedArrivalDate());
            itemVO.setProductSimpleInfo(productSimpleInfoVOMap.get(itemVO.getProductId()));
            itemVO.setSku(itemVO.getProductSimpleInfo().getSkuId());
            itemVO.setSkuName(itemVO.getProductSimpleInfo().getSkuName());
            itemVO.setImage(itemVO.getProductSimpleInfo().getImage());

            ProductPurchaseDO productPurchaseDO = productPurchaseDOMap.get(itemVO.getProductId());
            if (Objects.nonNull(productPurchaseDO)) {
                itemVO.setPcs(productPurchaseDO.getQuantityPerBox());
                itemVO.setNumberOfBox((int) Math.ceil((double) itemVO.getPurchaseQuantity() / productPurchaseDO.getQuantityPerBox()));
                // 长宽高cm / 1000000 * 箱数=体积
                itemVO.setVolume(productPurchaseDO.getBoxLength().multiply(productPurchaseDO.getBoxWidth())
                        .multiply(productPurchaseDO.getBoxHeight()).divide(new BigDecimal("1000000"))
                        .multiply(new BigDecimal(itemVO.getNumberOfBox())).setScale(3, RoundingMode.HALF_UP));

                // 重量g / 1000 * 箱数=重量
                itemVO.setWeight(productPurchaseDO.getBoxWeight().divide(new BigDecimal("1000"))
                        .multiply(new BigDecimal(itemVO.getNumberOfBox())).setScale(3, RoundingMode.HALF_UP));
            }

        });

        itemVOList.sort(Comparator.comparing(PurchaseOrderItemVO::getProductId));
        return itemVOList;
    }

    public List<PurchaseOrderItemVO> buildPurchaseOrderItemVO(List<PurchaseOrderItemDO> purchaseOrderItemList) {
        List<Long> orderIds = convertList(purchaseOrderItemList, PurchaseOrderItemDO::getOrderId);
        Map<Long, PurchaseOrderDO> orderMap = getOrderMap(orderIds);
        Map<Long, Integer> arrivedQuantityMap = getArrivedQuantityMap(orderIds);
        Map<Long, Integer> transportQuantityMap = getTransportQuantityMap(purchaseOrderItemList);
        List<Long> createUserIds = getCreateUserIds(purchaseOrderItemList);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(createUserIds);
        Map<String, PurchasePlanItemDO> planItemMap = getPlanItemMap(purchaseOrderItemList);
        List<Long> productIds = convertList(purchaseOrderItemList, PurchaseOrderItemDO::getProductId);
        Map<Long, ProductSimpleInfoVO> productSimpleInfoVOMap = productInfoService.batchQueryProductSimpleInfo(productIds);

        return BeanUtils.toBean(purchaseOrderItemList, PurchaseOrderItemVO.class, itemVO -> {
            setCommonFields(itemVO, userMap, arrivedQuantityMap, planItemMap);
            itemVO.setOrderNo(orderMap.get(itemVO.getOrderId()).getOrderNo());
            Integer totalShippedQuantity = transportQuantityMap.get(itemVO.getId());
            if (Objects.isNull(totalShippedQuantity)) {
                itemVO.setTotalShippedQuantity(0);
                itemVO.setRemainingQuantity(itemVO.getTotalArrivedQuantity());
            } else {
                itemVO.setTotalShippedQuantity(totalShippedQuantity);
                itemVO.setRemainingQuantity(itemVO.getTotalArrivedQuantity() - totalShippedQuantity);
            }
            ProductSimpleInfoVO productSimpleInfoVO = productSimpleInfoVOMap.get(itemVO.getProductId());
            if (Objects.nonNull(productSimpleInfoVO)) {
                itemVO.setProductSimpleInfo(productSimpleInfoVO);
            }
        });
    }

    private Map<Long, Integer> getArrivedQuantityMap(List<Long> orderIds) {
        List<PurchaseOrderArrivedLogDO> arrivedLogList = purchaseOrderService.batchPurchaseOrderArrivedLogListByPurchaseOrderIds(orderIds);
        return arrivedLogList.stream()
                .collect(Collectors.groupingBy(
                        PurchaseOrderArrivedLogDO::getPurchaseOrderItemId,
                        Collectors.summingInt(PurchaseOrderArrivedLogDO::getArrivedQuantity)
                ));
    }

    private List<Long> getCreateUserIds(List<PurchaseOrderItemDO> purchaseOrderItemList) {
        List<String> planNumbers = convertList(purchaseOrderItemList, PurchaseOrderItemDO::getPlanNumber);
        List<PurchasePlanItemDO> purchasePlanItemList = purchasePlanService.batchPurchasePlanItemListByPlanNumbers(planNumbers);
        List<Long> operateUserIds = convertListByFlatMap(purchasePlanItemList, purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator())));
        List<Long> createUserIds = convertListByFlatMap(purchaseOrderItemList, purchaseOrderDO -> Stream.of(NumberUtils.parseLong(purchaseOrderDO.getCreator())));
        createUserIds.addAll(operateUserIds);
        return createUserIds;
    }

    private Map<String, PurchasePlanItemDO> getPlanItemMap(List<PurchaseOrderItemDO> purchaseOrderItemList) {
        List<String> planNumbers = convertList(purchaseOrderItemList, PurchaseOrderItemDO::getPlanNumber);
        List<PurchasePlanItemDO> purchasePlanItemList = purchasePlanService.batchPurchasePlanItemListByPlanNumbers(planNumbers);
        return convertMap(purchasePlanItemList, PurchasePlanItemDO::getPlanNumber);
    }

    private Map<Long, PurchaseOrderDO> getOrderMap(List<Long> orderIds) {
        List<PurchaseOrderDO> purchaseOrderList = purchaseOrderService.getPurchaseOrderList(orderIds);
        return convertMap(purchaseOrderList, PurchaseOrderDO::getId);
    }

    private Map<Long, Integer> getTransportQuantityMap(List<PurchaseOrderItemDO> purchaseOrderItemList) {
        List<Long> purchaseOrderItemIds = convertList(purchaseOrderItemList, PurchaseOrderItemDO::getId);
        List<TransportPlanItemDO> transportPlanItemDOList = transportPlanService.batchTransportPlanItemListByPurchaseOrderItemIds(purchaseOrderItemIds);
        return transportPlanItemDOList.stream()
                .collect(Collectors.groupingBy(
                        TransportPlanItemDO::getPurchaseOrderItemId,
                        Collectors.summingInt(TransportPlanItemDO::getQuantity)
                ));
    }

    private void setCommonFields(PurchaseOrderItemVO itemVO, Map<Long, AdminUserRespDTO> userMap, Map<Long, Integer> arrivedQuantityMap, Map<String, PurchasePlanItemDO> planItemMap) {
        MapUtils.findAndThen(userMap, Long.parseLong(itemVO.getCreator()), user -> itemVO.setCreatorName(user.getNickname()));
        MapUtils.findAndThen(arrivedQuantityMap, itemVO.getId(), arrivedQuantity -> itemVO.setTotalArrivedQuantity(arrivedQuantity));
        Integer totalArrivedQuantity = arrivedQuantityMap.get(itemVO.getId());
        if (Objects.isNull(totalArrivedQuantity)) {
            itemVO.setRemainingQuantity(itemVO.getPurchaseQuantity());
            itemVO.setTotalArrivedQuantity(0);
        } else {
            itemVO.setRemainingQuantity(itemVO.getPurchaseQuantity() - totalArrivedQuantity);
        }

        PurchasePlanItemDO purchasePlanItemDO = planItemMap.get(itemVO.getPlanNumber());
        MapUtils.findAndThen(userMap, Long.parseLong(purchasePlanItemDO.getCreator()), user -> itemVO.setOperateName(user.getNickname()));
    }

}
