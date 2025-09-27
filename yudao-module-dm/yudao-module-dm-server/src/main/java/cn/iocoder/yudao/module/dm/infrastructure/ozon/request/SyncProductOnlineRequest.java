package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author zeno
 * @Date 2024/2/9
 */
@Data
public class SyncProductOnlineRequest extends HttpBaseRequest {

    @JSONField(name = "offer_id")
    private List<String> offerId;
    @JSONField(name = "product_id")
    private List<String> productId;
    @JSONField(name = "limit")
    private Integer limit;
    @JSONField(name= "filter")
    private Map<String, Object> filter;
}
