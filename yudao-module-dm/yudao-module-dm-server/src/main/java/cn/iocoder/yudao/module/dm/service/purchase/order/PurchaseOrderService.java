package cn.iocoder.yudao.module.dm.service.purchase.order;

import java.math.BigDecimal;
import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog.PurchaseOrderArrivedLogDO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 采购单 Service 接口
 *
 * @author Zeno
 */
public interface PurchaseOrderService {

    /**
     * 创建采购单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseOrder(@Valid PurchaseOrderSaveReqVO createReqVO);

    /**
     * 更新采购单
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseOrder(@Valid PurchaseOrderSaveReqVO updateReqVO);

    /**
     * 强制更新采购单
     *
     * @param purchaseOrderId
     * @param purchaseOrderItemId
     */
    void forceUpdatePurchaseOrder(Long purchaseOrderId, Long purchaseOrderItemId);

    /**
     * 更新付款金额
     *
     * @param id
     * @param paymentPrice
     */
    void updatePaymentPrice(Long id, BigDecimal paymentPrice);

    /**
     * 更新到货信息
     *
     * @param id
     * @param arrivedQuantity
     */
    void updateArriveQuantity(Long id, Integer arrivedQuantity);

    /**
     * 更新采购单状态
     *
     * @param id
     * @param status
     */
    void updatePurchaseOrderStatus(Long id, Integer status);

    /**
     * 更新采购单备注
     *
     * @param id
     * @param remark
     */
    void updatePurchaseOrderRemark(Long id, String remark);

    /**
     * 删除采购单
     *
     * @param id 编号
     */
    void deletePurchaseOrder(Long id);

    /**
     * 获得采购单
     *
     * @param id 编号
     * @return 采购单
     */
    PurchaseOrderDO getPurchaseOrder(Long id);

    /**
     * 获得采购单列表
     *
     * @param ids
     * @return
     */
    List<PurchaseOrderDO> getPurchaseOrderList(Collection<Long> ids);

    /**
     * 批量获取采购单
     *
     * @param orderNos
     * @return
     */
    List<PurchaseOrderDO> batchPurchaseOrderList(Collection<String> orderNos);

    /**
     * 获得采购单分页
     *
     * @param pageReqVO 分页查询
     * @return 采购单分页
     */
    PageResult<PurchaseOrderDO> getPurchaseOrderPage(PurchaseOrderPageReqVO pageReqVO);

    /**
     * 获得采购单分页2
     *
     * @param page
     * @param pageReqVO
     * @return
     */
    IPage<PurchaseOrderDO> getPurchaseOrderPage2(IPage<PurchaseOrderDO> page, PurchaseOrderPageReqVO pageReqVO);

    /**
     * 批量更新采购单状态
     * @param purchaseOrders
     */
    void batchUpdatePurchaseOrderStatus(List<PurchaseOrderDO> purchaseOrders);

    // ==================== 子表（采购单明细） ====================

    /**
     * 获得采购单明细列表
     *
     * @param orderId 采购单Id
     * @return 采购单明细列表
     */
    List<PurchaseOrderItemDO> getPurchaseOrderItemListByOrderId(Long orderId);

    /**
     * 批量获取采购单明细
     *
     * @param orderIds
     * @return
     */
    List<PurchaseOrderItemDO> batchPurchaseOrderItemListByOrderIds(Collection<Long> orderIds);

    /**
     * 获取采购单明细
     *
     * @param planNumber
     * @return
     */
    PurchaseOrderItemDO getPurchaseOrderItemByPlanNumber(String planNumber);

    /**
     * 根据采购单明细ID获取采购单明细
     *
     * @param purchaseOrderItemId
     * @return
     */
    PurchaseOrderItemDO getPurchaseOrderItemById(Long purchaseOrderItemId);

    /**
     * 根据产品ID获取采购单明细列表
     *
     * @param productIds
     * @return
     */
    List<PurchaseOrderItemDO> batchPurchaseItemListByProductIds(Collection<Long> productIds);

    /**
     * 批量获取采购单明细
     *
     * @param planNumbers
     * @return
     */
    List<PurchaseOrderItemDO> batchPurchaseItemListByPlanNumbers(Collection<String> planNumbers);

    /**
     * 批量获取采购单明细
     *
     * @param ids
     * @return
     */
    List<PurchaseOrderItemDO> batchPurchaseItemListByIds(Collection<Long> ids);

    /**
     * 获得采购单明细分页
     *
     * @param pageReqVO
     * @return
     */
    PageResult<PurchaseOrderItemDO> getPurchaseOrderItemPage(PurchaseOrderItemPageReqVO pageReqVO);

    // ==================== 子表（采购单到货日志） ====================

    /**
     * 获得采购单到货日志列表
     *
     * @param purchaseOrderId 采购单ID
     * @return 采购单到货日志列表
     */
    List<PurchaseOrderArrivedLogDO> getPurchaseOrderArrivedLogListByPurchaseOrderId(Long purchaseOrderId);

    /**
     * 批量获得采购单到货日志列表
     *
     * @param purchaseOrderIds
     * @return
     */
    List<PurchaseOrderArrivedLogDO> batchPurchaseOrderArrivedLogListByPurchaseOrderIds(Collection<Long> purchaseOrderIds);
}