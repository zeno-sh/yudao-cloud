package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Ozon 供应订单列表请求
 *
 * @author Zeno
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SupplyOrderListRequest extends HttpBaseRequest {
    @JSONField(name = "filter")
    private Filter filter;
    @JSONField(name = "paging")
    private Paging paging;

    @Data
    public static class Filter {
        @JSONField(name = "states")
        private List<String> states;
    }

    @Data
    public static class Paging {
        @JSONField(name = "from_supply_order_id")
        private Long fromSupplyOrderId;
        @JSONField(name = "limit")
        private Integer limit = 100;
    }
} 