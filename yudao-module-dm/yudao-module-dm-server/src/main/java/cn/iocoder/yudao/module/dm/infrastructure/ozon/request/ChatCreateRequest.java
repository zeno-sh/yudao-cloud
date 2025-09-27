package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/13
 */
@Data
public class ChatCreateRequest extends HttpBaseRequest {
    @JSONField(name = "posting_number")
    private String postingNumber;
}
