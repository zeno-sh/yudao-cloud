package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.FinanceFilter;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/1/30
 */
@Data
public class FinanceTransactionRequest extends HttpBaseRequest {
    private Integer page;
    @JSONField(name = "page_size")
    private Integer pageSize;
    private FinanceFilter filter;
}


