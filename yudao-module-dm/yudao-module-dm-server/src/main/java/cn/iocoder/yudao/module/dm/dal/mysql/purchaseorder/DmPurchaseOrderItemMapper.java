package cn.iocoder.yudao.module.dm.dal.mysql.purchaseorder;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo.PurchaseOrderItemPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderItemDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanItemDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购单明细 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface DmPurchaseOrderItemMapper extends BaseMapperX<PurchaseOrderItemDO> {

    default List<PurchaseOrderItemDO> selectListByOrderId(Long orderId) {
        return selectList(PurchaseOrderItemDO::getOrderId, orderId);
    }

    default int deleteByOrderId(Long orderId) {
        return delete(PurchaseOrderItemDO::getOrderId, orderId);
    }

    default PageResult<PurchaseOrderItemDO> selectPageOld(PurchaseOrderItemPageReqVO reqVO) {

        LambdaQueryWrapperX<PurchaseOrderItemDO> query = new LambdaQueryWrapperX<PurchaseOrderItemDO>()
                .eqIfPresent(PurchaseOrderItemDO::getProductId, reqVO.getProductId())
                .eqIfPresent(PurchaseOrderItemDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(PurchaseOrderItemDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PurchaseOrderItemDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PurchaseOrderItemDO::getId);

        if (Boolean.TRUE.equals(reqVO.getShippedEnable())) {
            query.apply(" arrive_quantity - shipped_quantity >= 0");
        }

        MPJLambdaWrapper<PurchaseOrderItemDO> wrapper = new MPJLambdaWrapper<>();
        wrapper.selectAll(PurchaseOrderItemDO.class)
                .leftJoin(TransportPlanItemDO.class, TransportPlanItemDO::getPurchaseOrderItemId, PurchaseOrderItemDO::getId)
                .groupBy(PurchaseOrderItemDO::getId);

        // 动态添加过滤条件
        if (reqVO.getOrderId() != null) {
            wrapper.eq(PurchaseOrderItemDO::getOrderId, reqVO.getOrderId());
        }
        if (reqVO.getProductId() != null) {
            wrapper.eq(PurchaseOrderItemDO::getProductId, reqVO.getProductId());
        }
        if (reqVO.getStatus() != null) {
            wrapper.eq(PurchaseOrderItemDO::getStatus, reqVO.getStatus());
        }
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length == 2) {
            wrapper.between(PurchaseOrderItemDO::getCreateTime, reqVO.getCreateTime()[0], reqVO.getCreateTime()[1]);
        }

        // 当shippedEnable为true时，添加HAVING子句
        if (Boolean.TRUE.equals(reqVO.getShippedEnable())) {
            wrapper.having("SUM(t.arrive_quantity) - IFNULL(SUM(t1.quantity), 0) > 0");
        }

//        return selectPage(reqVO, query);
        return selectJoinPage(reqVO, PurchaseOrderItemDO.class, wrapper);
    }

    default PageResult<PurchaseOrderItemDO> selectPage(PurchaseOrderItemPageReqVO reqVO) {

        MPJLambdaWrapper<PurchaseOrderItemDO> wrapper = new MPJLambdaWrapper<>();

        wrapper.select("t.id, t.order_id, t.product_id, t.pcs, t.price, t.tax_price, t.tax_rate, t.tax, t.purchase_quantity, t.arrival_date, t.remark, t.tax_amount, t.total_amount, t.total_tax_amount, t.plan_number, t.status, t.create_time, t.update_time, t.creator, t.updater, t.deleted")
                .select("COALESCE(SUM(t1.arrived_quantity), 0) AS total_arrived_quantity")
                .select("COALESCE(SUM(t2.quantity), 0) AS total_shipped_quantity")
                .select("(COALESCE(SUM(t1.arrived_quantity), 0) - COALESCE(SUM(t2.quantity), 0)) AS remaining_quantity")
                .leftJoin("(SELECT purchase_order_item_id,SUM(arrived_quantity) AS arrived_quantity FROM dm_purchase_order_arrived_log WHERE deleted = b'0' GROUP BY purchase_order_item_id) t1 ON t.id=t1.purchase_order_item_id")
                .leftJoin("(SELECT purchase_order_item_id,SUM(quantity) AS quantity FROM dm_transport_plan_item  WHERE deleted = b'0' GROUP BY purchase_order_item_id) t2 ON t.id=t2.purchase_order_item_id ")
                .groupBy(PurchaseOrderItemDO::getId)
                .orderByAsc(PurchaseOrderItemDO::getProductId);

        // 动态添加过滤条件
        if (reqVO.getOrderId() != null) {
            wrapper.eq(PurchaseOrderItemDO::getOrderId, reqVO.getOrderId());
        }
        if (reqVO.getProductId() != null) {
            wrapper.eq(PurchaseOrderItemDO::getProductId, reqVO.getProductId());
        }
        if (reqVO.getStatus() != null) {
            wrapper.eq(PurchaseOrderItemDO::getStatus, reqVO.getStatus());
        }
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length == 2) {
            wrapper.between(PurchaseOrderItemDO::getCreateTime, reqVO.getCreateTime()[0], reqVO.getCreateTime()[1]);
        }

        // 当shippedEnable为true时，添加HAVING子句，筛选未到货的记录
        if (Boolean.TRUE.equals(reqVO.getShippedEnable())) {
            wrapper.having("remaining_quantity > 0");
        }

        return selectJoinPage(reqVO, PurchaseOrderItemDO.class, wrapper);
    }
}