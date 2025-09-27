package cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request;

import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbHttpBaseRequest;
import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/07/10 23:24
 */
@Data
public class WbOrderQueryRequest extends WbHttpBaseRequest {

    /**
     * 每页数量，最大值1000
     */
    private Integer limit;
    /**
     * 分页参数，上个请求会返回
     */
    private Long next;
    /**
     * 开始时间，UTC时间戳，最大30天
     */
    private Long dateFrom;
    /**
     * 截止时间
     */
    private Long dateTo;
}
