package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/1/28
 */
@Data
public class HttpBaseRequest {

    @JSONField(name = "Client-Id")
    private String clientId;
    @JSONField(name = "Api-Key")
    private String apiKey;
    private String beginDate;
    private String endDate;
}
