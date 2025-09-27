package cn.iocoder.yudao.module.dm.infrastructure.ozon.request;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.PriceFilterDTO;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/15
 */
@Data
public class PriceRequest extends HttpBaseRequest{

    private PriceFilterDTO filter;
    private Integer limit;
    @JSONField(name="last_id")
    private String lastId;
}
