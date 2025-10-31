package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.IncludeDTO;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/13
 */
@Data
public class FbsOrderInfoRequest extends HttpBaseRequest{

    @JSONField(name = "posting_number")
    private String postingNumber;
    @JSONField(name = "with")
    private IncludeDTO include;
}
