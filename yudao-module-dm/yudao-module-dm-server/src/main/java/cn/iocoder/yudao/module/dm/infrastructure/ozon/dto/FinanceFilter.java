package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/1/31
 */
@Data
public class FinanceFilter {
    @JSONField(name = "posting_number")
    private String postingNumber;
    @JSONField(name = "transaction_type")
    private String transactionType;
    private DateDTO date;
    @JSONField(name = "operation_type")
    private List<String> operation_type;
}
