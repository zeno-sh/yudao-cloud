package cn.iocoder.yudao.module.dm.infrastructure.ozon.response;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * Ozon 供应订单列表响应
 *
 * @author Zeno
 */
@Data
public class SupplyOrderListResponse {
    @JSONField(name = "last_supply_order_id")
    private Long lastSupplyOrderId;
    @JSONField(name = "supply_order_id")
    private List<String> supplyOrderIds;
} 