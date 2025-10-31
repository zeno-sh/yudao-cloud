package cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request;

import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.Order;
import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/08/07 16:49
 */
@Data
public class WbFboOrderQueryRequest extends WbHttpBaseRequest {
    /**
     * see https://openapi.wildberries.ru/statistics/api/ru/#tag/Statistika/paths/~1api~1v1~1supplier~1orders/get
     */
    private Integer flag;

    /**
     * 2019-06-20T23:59:59Z格式，UTC+3时区
     */
    private String dateFrom;

    /**
     * 2019-06-20T23:59:59Z格式，UTC+3时区
     */
    private String dateTo;

    /**
     * fbs订单，对比用
     */
    private List<Order> fbsOrderList;
}
