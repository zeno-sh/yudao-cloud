package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.AcceptedDateDTO;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/1/28
 */
@Data
public class OrderReturnRequest extends HttpBaseRequest {

    @JSONField(name = "order_id")
    private String orderId;
    @JSONField(name = "posting_number")
    private String postingNumber;
    @JSONField(name = "product_offer_id")
    private String offerId;
    @JSONField(name = "accepted_from_customer_moment")
    private AcceptedDateDTO acceptedDate;
    @JSONField(name = "limit")
    private Integer limit;
    @JSONField(name = "last_id")
    private Integer lastId;

}

