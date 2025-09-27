package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.DateDTO;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/1/28
 */
@Data
public class OrderFinanceRequest extends HttpBaseRequest {
    @JSONField(name = "order_id")
    private String orderId;
    @JSONField(name = "posting_number")
    private String postingNumber;
    @JSONField(name = "transaction_type")
    private String transactionType;
    private DateDTO date;
    @JSONField(name = "operation_type")
    private List<String> operation_type;


}

