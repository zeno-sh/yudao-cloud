package cn.iocoder.yudao.module.dm.service.purchase.order;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchase.PurchasePlanItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog.PurchaseOrderArrivedLogDO;
import cn.iocoder.yudao.module.dm.dal.mysql.purchase.PurchasePlanItemMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.purchase.PurchasePlanMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.purchaseorderlog.PurchaseOrderArrivedLogMapper;
import cn.iocoder.yudao.module.dm.dal.redis.dao.DmNoRedisDAO;
import cn.iocoder.yudao.module.dm.enums.PurchaseOrderStatusEnum;
import cn.iocoder.yudao.module.dm.enums.PurchasePlanStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.purchaseorder.DmPurchaseOrderMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.purchaseorder.DmPurchaseOrderItemMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.PURCHASE_ORDER_NOT_EXISTS;

/**
 * 采购单 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Resource
    private DmPurchaseOrderMapper dmPurchaseOrderMapper;
    @Resource
    private DmPurchaseOrderItemMapper dmPurchaseOrderItemMapper;
    @Resource
    private DmNoRedisDAO dmNoRedisDAO;
    @Resource
    private PurchasePlanMapper purchasePlanMapper;
    @Resource
    private PurchasePlanItemMapper purchasePlanItemMapper;
    @Resource
    private PurchaseOrderArrivedLogMapper purchaseOrderArrivedLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseOrder(PurchaseOrderSaveReqVO createReqVO) {
        // 插入
        PurchaseOrderDO purchaseOrder = BeanUtils.toBean(createReqVO, PurchaseOrderDO.class);
        purchaseOrder.setOrderNo(dmNoRedisDAO.generate(DmNoRedisDAO.PURCHASE_PO_NO_PREFIX));
        calculateTotalAmount(purchaseOrder, createReqVO.getPurchaseOrderItems());
        // 处理单据负责人
        handleOwner(createReqVO, purchaseOrder);
        dmPurchaseOrderMapper.insert(purchaseOrder);

        // 插入子表
        createPurchaseOrderItemList(purchaseOrder.getId(), createReqVO.getPurchaseOrderItems());

        // 变更采购计划状态（如果关联了采购计划，状态需要更新）
        // 2024-08-05 注释掉（因为需要根据是否下单来作废之前的数据，所以不能直接修改状态）
//        handlePlanStatus(createReqVO.getPurchaseOrderItems(), PurchasePlanStatusEnum.SUCCESS.getStatus());
        // 返回
        return purchaseOrder.getId();
    }

    private void calculateTotalAmount(PurchaseOrderDO purchaseOrder, List<PurchaseOrderItemDO> purchaseOrderItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        Integer totalCount = 0;
        for (PurchaseOrderItemDO purchaseOrderItem : purchaseOrderItems) {
            if (purchaseOrder.getTax()) {
                totalAmount = totalAmount.add(purchaseOrderItem.getTotalTaxAmount());
                totalTaxAmount = totalTaxAmount.add(purchaseOrderItem.getTaxAmount());
            } else {
                totalAmount = totalAmount.add(purchaseOrderItem.getTotalAmount());
            }
            totalCount += purchaseOrderItem.getPurchaseQuantity();
        }
        purchaseOrder.setTotalProductPrice(totalAmount);
        purchaseOrder.setTotalPrice(totalAmount.add(purchaseOrder.getTransportationPrice()).add(purchaseOrder.getOtherPrice()));
        purchaseOrder.setTotalTaxPrice(totalTaxAmount);
        purchaseOrder.setTotalCount(totalCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrder(PurchaseOrderSaveReqVO updateReqVO) {
        // 校验存在
        validatePurchaseOrderExists(updateReqVO.getId());
        // 更新
        PurchaseOrderDO updateObj = BeanUtils.toBean(updateReqVO, PurchaseOrderDO.class);
        // 计算各种金额
        calculateTotalAmount(updateObj, updateReqVO.getPurchaseOrderItems());
        // 处理单据负责人
        handleOwner(updateReqVO, updateObj);
        // 变更采购计划状态
        handlePurchaseOrderStatus(updateObj);

        dmPurchaseOrderMapper.updateById(updateObj);

        // 更新子表
        updatePurchaseOrderItemList(updateReqVO.getId(), updateReqVO.getPurchaseOrderItems());
    }

    @Override
    public void forceUpdatePurchaseOrder(Long purchaseOrderId, Long purchaseOrderItemId) {
        // 校验存在
        validatePurchaseOrderExists(purchaseOrderId);
        PurchaseOrderDO purchaseOrder = getPurchaseOrder(purchaseOrderId);
        List<PurchaseOrderItemDO> purchaseOrderItemList = getPurchaseOrderItemListByOrderId(purchaseOrderId);

        List<PurchaseOrderItemDO> updateItemList = new ArrayList<>();

        // 查询已到货数量，强制修改采购数量与已到货数量相同
        List<PurchaseOrderArrivedLogDO> purchaseOrderArrivedLogList = getPurchaseOrderArrivedLogListByPurchaseOrderId(purchaseOrderId);
        int arriveQuantity = purchaseOrderArrivedLogList.stream()
                .filter(purchaseOrderArrivedLogDO -> purchaseOrderArrivedLogDO.getPurchaseOrderItemId().equals(purchaseOrderItemId))
                .map(PurchaseOrderArrivedLogDO::getArrivedQuantity)
                .mapToInt(Integer::intValue)
                .sum();

        for (PurchaseOrderItemDO purchaseOrderItem : purchaseOrderItemList) {
            if (purchaseOrderItem.getId().equals(purchaseOrderItemId)) {
                purchaseOrderItem.setPurchaseQuantity(arriveQuantity);

                purchaseOrderItem.setTotalAmount(purchaseOrderItem.getPrice().multiply(new BigDecimal(arriveQuantity)));
                if (purchaseOrder.getTax()) {
                    purchaseOrderItem.setTotalTaxAmount(purchaseOrderItem.getTaxPrice().multiply(new BigDecimal(arriveQuantity)));
                }

                dmPurchaseOrderItemMapper.updateById(purchaseOrderItem);
            }
            updateItemList.add(purchaseOrderItem);
        }

        // 计算各种金额
        calculateTotalAmount(purchaseOrder, updateItemList);
        // 变更采购计划状态
        handlePurchaseOrderStatus(purchaseOrder);

        dmPurchaseOrderMapper.updateById(purchaseOrder);

    }

    private void handlePurchaseOrderStatus(PurchaseOrderDO updateObj) {
        Integer totalCount = updateObj.getTotalCount();
        Integer arriveQuantity = updateObj.getArriveQuantity();
        if (arriveQuantity.equals(totalCount)) {
            updateObj.setStatus(PurchaseOrderStatusEnum.SUCCESS.getStatus());
        }

        if (arriveQuantity > 0 && arriveQuantity < totalCount) {
            updateObj.setStatus(PurchaseOrderStatusEnum.PARTIAL_ARRIVE.getStatus());
        }
    }

    @Override
    public void updatePaymentPrice(Long id, BigDecimal paymentPrice) {
        PurchaseOrderDO purchaseOrderDO = dmPurchaseOrderMapper.selectById(id);
        if (purchaseOrderDO.getPaymentPrice().equals(paymentPrice)) {
            return;
        }
        if (paymentPrice.compareTo(purchaseOrderDO.getTotalPrice()) > 0) {
            throw exception(PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED, paymentPrice, purchaseOrderDO.getTotalPrice());
        }
        dmPurchaseOrderMapper.updateById(new PurchaseOrderDO().setId(id).setPaymentPrice(paymentPrice));
    }

    @Override
    public void updateArriveQuantity(Long id, Integer arrivedQuantity) {
        dmPurchaseOrderMapper.updateById(new PurchaseOrderDO().setId(id).setArriveQuantity(arrivedQuantity));
    }

    //
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void updateArriveQuantity(@Valid PurchaseArriveSaveReqVO updateReqVO) {
//        // 校验存在
//        validatePurchaseOrderExists(updateReqVO.getId());
//        PurchaseOrderDO updateObj = BeanUtils.toBean(updateReqVO, PurchaseOrderDO.class);
//        List<PurchaseOrderItemVO> purchaseOrderItems = updateReqVO.getPurchaseOrderItems();
//        int totalArriveQuantity = purchaseOrderItems.stream().mapToInt(item -> item.getArriveQuantity() + item.getCurrentArriveQuantity()).sum();
//        updateObj.setArriveQuantity(totalArriveQuantity);
//
//        // 初始化一个标志变量，用于判断所有子项是否都已到货
//        boolean allItemsArrived = false;
//
//        // 遍历所有采购订单项，更新每个项的状态
//        List<PurchaseOrderItemDO> purchaseOrderItemDOList = new ArrayList<>();
//        for (PurchaseOrderItemVO item : purchaseOrderItems) {
//            PurchaseOrderItemDO itemDO = BeanUtils.toBean(item, PurchaseOrderItemDO.class);
//            int arriveQuantity = item.getArriveQuantity() + item.getCurrentArriveQuantity();
//            if (arriveQuantity >= item.getPurchaseQuantity()) {
//                itemDO.setStatus(PurchaseOrderStatusEnum.SUCCESS.getStatus());
//                allItemsArrived = true;
//            }
//            itemDO.setArriveQuantity(arriveQuantity);
//            purchaseOrderItemDOList.add(itemDO);
//        }
//
//        // 如果所有采购订单项都已到货，则更新采购订单的状态为“已到货”
//        if (allItemsArrived) {
//            updateObj.setStatus(PurchaseOrderStatusEnum.SUCCESS.getStatus());
//        }
//
//        dmPurchaseOrderMapper.updateById(updateObj);
//        dmPurchaseOrderItemMapper.updateBatch(purchaseOrderItemDOList);
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrderStatus(Long id, Integer status) {
        // 校验存在
        validatePurchaseOrderExists(id);

        dmPurchaseOrderMapper.updateById(new PurchaseOrderDO().setId(id).setStatus(status));

        List<PurchaseOrderItemDO> purchaseOrderItemDOS = dmPurchaseOrderItemMapper.selectList(PurchaseOrderItemDO::getOrderId, id);
        List<Long> itemIds = CollectionUtils.convertList(purchaseOrderItemDOS, PurchaseOrderItemDO::getId);
        dmPurchaseOrderItemMapper.update(new PurchaseOrderItemDO().setStatus(status),
                new LambdaUpdateWrapper<PurchaseOrderItemDO>()
                        .in(PurchaseOrderItemDO::getId, itemIds));

        if (!PurchaseOrderStatusEnum.DO_DELETED.getStatus().equals(status)
                && !PurchaseOrderStatusEnum.DO_ORDER.getStatus().equals(status)) {
            handlePlanStatus(purchaseOrderItemDOS, PurchasePlanStatusEnum.SUCCESS.getStatus());
        }
    }

    @Override
    public void updatePurchaseOrderRemark(Long id, String remark) {
        // 校验存在
        validatePurchaseOrderExists(id);

        dmPurchaseOrderMapper.updateById(new PurchaseOrderDO().setId(id).setRemark(remark));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseOrder(Long id) {
        // 校验存在
        validatePurchaseOrderExists(id);
        // 删除
        dmPurchaseOrderMapper.deleteById(id);

        // 删除子表
        deletePurchaseOrderItemByOrderId(id);
    }

    private void validatePurchaseOrderExists(Long id) {
        if (dmPurchaseOrderMapper.selectById(id) == null) {
            throw exception(PURCHASE_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public PurchaseOrderDO getPurchaseOrder(Long id) {
        return dmPurchaseOrderMapper.selectById(id);
    }

    @Override
    public List<PurchaseOrderDO> getPurchaseOrderList(Collection<Long> ids) {
        return dmPurchaseOrderMapper.selectList(PurchaseOrderDO::getId, ids);
    }

    @Override
    public List<PurchaseOrderDO> batchPurchaseOrderList(Collection<String> orderNos) {
        return dmPurchaseOrderMapper.selectList(PurchaseOrderDO::getOrderNo, orderNos);
    }

    @Override
    public PageResult<PurchaseOrderDO> getPurchaseOrderPage(PurchaseOrderPageReqVO pageReqVO) {
        return dmPurchaseOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public IPage<PurchaseOrderDO> getPurchaseOrderPage2(IPage<PurchaseOrderDO> page, PurchaseOrderPageReqVO pageReqVO) {
        return dmPurchaseOrderMapper.selectPage2(page, pageReqVO);
    }

    @Override
    public void batchUpdatePurchaseOrderStatus(List<PurchaseOrderDO> purchaseOrders) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(purchaseOrders)) {
            return;
        }
        for (PurchaseOrderDO purchaseOrder : purchaseOrders) {
            handlePurchaseOrderStatus(purchaseOrder);
        }
        dmPurchaseOrderMapper.updateBatch(purchaseOrders);
    }

    // ==================== 子表（采购单明细） ====================

    @Override
    public List<PurchaseOrderItemDO> getPurchaseOrderItemListByOrderId(Long orderId) {
        return dmPurchaseOrderItemMapper.selectListByOrderId(orderId);
    }

    @Override
    public List<PurchaseOrderItemDO> batchPurchaseOrderItemListByOrderIds(Collection<Long> orderIds) {
        return dmPurchaseOrderItemMapper.selectList(PurchaseOrderItemDO::getOrderId, orderIds);
    }

    @Override
    @DataPermission(enable = false) // 这里不需要权限
    public PurchaseOrderItemDO getPurchaseOrderItemByPlanNumber(String planNumber) {
        return dmPurchaseOrderItemMapper.selectOne(PurchaseOrderItemDO::getPlanNumber, planNumber);
    }

    @Override
    public PurchaseOrderItemDO getPurchaseOrderItemById(Long purchaseOrderItemId) {
        return dmPurchaseOrderItemMapper.selectOne(PurchaseOrderItemDO::getOrderId, purchaseOrderItemId);
    }

    @Override
    public List<PurchaseOrderItemDO> batchPurchaseItemListByProductIds(Collection<Long> productIds) {
        return dmPurchaseOrderItemMapper.selectList(PurchaseOrderItemDO::getProductId, productIds);
    }

    @Override
    public List<PurchaseOrderItemDO> batchPurchaseItemListByPlanNumbers(Collection<String> planNumbers) {
        return  dmPurchaseOrderItemMapper.selectList(PurchaseOrderItemDO::getPlanNumber, planNumbers);
    }

    @Override
    public List<PurchaseOrderItemDO> batchPurchaseItemListByIds(Collection<Long> ids) {
        return dmPurchaseOrderItemMapper.selectList(PurchaseOrderItemDO::getId, ids);
    }

    @Override
    public PageResult<PurchaseOrderItemDO> getPurchaseOrderItemPage(PurchaseOrderItemPageReqVO pageReqVO) {
        if (StringUtils.isNotBlank(pageReqVO.getOrderNo())) {
            PurchaseOrderDO purchaseOrderDO = dmPurchaseOrderMapper.selectOne(PurchaseOrderDO::getOrderNo, pageReqVO.getOrderNo());
            if (Objects.nonNull(purchaseOrderDO)) {
                pageReqVO.setOrderId(purchaseOrderDO.getId());
            }
        }
        return dmPurchaseOrderItemMapper.selectPage(pageReqVO);
    }

    private void createPurchaseOrderItemList(Long orderId, List<PurchaseOrderItemDO> list) {
        list.forEach(o -> o.setOrderId(orderId));
        dmPurchaseOrderItemMapper.insertBatch(list);
    }

    private void updatePurchaseOrderItemList(Long orderId, List<PurchaseOrderItemDO> list) {
        deletePurchaseOrderItemByOrderId(orderId);
        list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createPurchaseOrderItemList(orderId, list);
    }

    private void deletePurchaseOrderItemByOrderId(Long orderId) {
        dmPurchaseOrderItemMapper.deleteByOrderId(orderId);
    }



    // ==================== 子表（采购单到货日志） ====================

    @Override
    public List<PurchaseOrderArrivedLogDO> getPurchaseOrderArrivedLogListByPurchaseOrderId(Long purchaseOrderId) {
        return purchaseOrderArrivedLogMapper.selectListByPurchaseOrderId(purchaseOrderId);
    }

    @Override
    public List<PurchaseOrderArrivedLogDO> batchPurchaseOrderArrivedLogListByPurchaseOrderIds(Collection<Long> purchaseOrderIds) {
        return purchaseOrderArrivedLogMapper.selectList(PurchaseOrderArrivedLogDO::getPurchaseOrderId, purchaseOrderIds);
    }

    private void createPurchaseOrderArrivedLogList(Long purchaseOrderId, List<PurchaseOrderArrivedLogDO> list) {
        list.forEach(o -> o.setPurchaseOrderId(purchaseOrderId));
        purchaseOrderArrivedLogMapper.insertBatch(list);
    }

    private void updatePurchaseOrderArrivedLogList(Long purchaseOrderId, List<PurchaseOrderArrivedLogDO> list) {
        deletePurchaseOrderArrivedLogByPurchaseOrderId(purchaseOrderId);
        list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createPurchaseOrderArrivedLogList(purchaseOrderId, list);
    }

    private void deletePurchaseOrderArrivedLogByPurchaseOrderId(Long purchaseOrderId) {
        purchaseOrderArrivedLogMapper.deleteByPurchaseOrderId(purchaseOrderId);
    }

    private void handleOwner(PurchaseOrderSaveReqVO createReqVO, PurchaseOrderDO purchaseOrder) {
        if (Objects.isNull(createReqVO.getOwner())) {
            purchaseOrder.setOwner(SecurityFrameworkUtils.getLoginUserId());
        }
    }

    private void handlePlanStatus(List<PurchaseOrderItemDO> purchaseOrderItems, Integer status) {
        // 1.先修改planItem表的状态
        List<String> planNumberList = CollectionUtils.convertList(purchaseOrderItems, PurchaseOrderItemDO::getPlanNumber);
        // 状态参见字典 dm_purchase_plan_status
        purchasePlanItemMapper.update(new PurchasePlanItemDO().setStatus(status),
                new LambdaUpdateWrapper<PurchasePlanItemDO>()
                        .in(PurchasePlanItemDO::getPlanNumber, planNumberList));

        // 2.然后根据planItem表的全部状态更新主表状态
        List<Integer> statuses = purchasePlanItemMapper.selectList(new LambdaQueryWrapper<PurchasePlanItemDO>()
                        .in(PurchasePlanItemDO::getPlanNumber, planNumberList))
                .stream()
                .map(PurchasePlanItemDO::getStatus)
                .distinct()
                .collect(Collectors.toList());

        // 3.根据子表状态更新主表状态
        Integer mainStatus = PurchasePlanStatusEnum.DO_PURCHASED.getStatus(); // 默认初始状态为0
        if (statuses.size() == 1) {
            // 作废状态参见字典 dm_purchase_order_status
            mainStatus = statuses.get(0);
        }
        List<Long> planIds = CollectionUtils.convertList(purchaseOrderItems, PurchaseOrderItemDO::getOrderId);
        purchasePlanMapper.update(new PurchasePlanDO().setStatus(mainStatus),
                new LambdaUpdateWrapper<PurchasePlanDO>()
                        .in(PurchasePlanDO::getId, planIds));
    }
}