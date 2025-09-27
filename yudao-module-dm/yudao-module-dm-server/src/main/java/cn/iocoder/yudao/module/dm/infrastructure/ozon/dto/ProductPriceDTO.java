package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/2/15
 */
@Data
public class ProductPriceDTO {

    private List<ProductPriceItemDTO> items;
    private Integer total;
    @JSONField(name="last_id")
    private String lastId;
}
