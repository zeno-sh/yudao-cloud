package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/1/28
 */
@Data
public class FinanceTransactionResultDTO {
    private List<FinanceTransactionDTO> operations;
    @JSONField(name="page_count")
    private Integer pageCount;
    @JSONField(name="row_count")
    private Integer rowCount;
}
