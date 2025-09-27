package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Ozon 供应订单详情请求
 *
 * @author Zeno
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SupplyOrderGetRequest extends HttpBaseRequest {
    @JSONField(name = "order_ids")
    private List<String> orderIds;
} 