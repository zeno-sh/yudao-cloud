package cn.iocoder.yudao.module.dm.service.profitreport.v2.model;

import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单数据
 *
 * @author Jax
 */
@Data
@Builder
public class OrderData {
    
    /**
     * 订单列表 - 这里存储的是从账单表posting_number查询出来的订单（已经是正确的订单）
     */
    private List<OzonOrderDO> orders;
    
    /**
     * 订单明细列表 - 对应上述订单的明细
     */
    private List<OzonOrderItemDO> orderItems;
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 订单总数
     */
    private Integer totalOrderCount;
    
    /**
     * 订单明细总数
     */
    private Integer totalItemCount;
    
    /**
     * 获取签收订单列表
     * 
     * 重要修改：不再按状态过滤，因为传入的orders已经是从账单表posting_number筛选出来的正确订单
     * 按照getSignedOrderList2的逻辑，从账单表提取的posting_number对应的订单就是需要计算的订单
     */
    public List<OzonOrderDO> getSignedOrders() {
        return orders != null ? orders : new ArrayList<>();
    }
    
    /**
     * 获取签收订单明细列表
     * 
     * 重要修改：直接返回对应的订单明细，不需要额外的状态过滤
     * 因为orderItems对应的就是从账单表posting_number查询出来的订单的明细
     */
    public List<OzonOrderItemDO> getSignedOrderItems() {
        return orderItems != null ? orderItems : new ArrayList<>();
    }
    
    /**
     * 获取所有订单明细
     */
    public List<OzonOrderItemDO> getAllOrderItems() {
        return orderItems != null ? orderItems : new ArrayList<>();
    }
}