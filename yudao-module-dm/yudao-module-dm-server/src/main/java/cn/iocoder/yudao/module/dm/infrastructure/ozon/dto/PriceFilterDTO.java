package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/2/15
 */
@Data
public class PriceFilterDTO {
    @JSONField(name = "offer_id")
    private List<String> offerId;

    private String visibility;

}
