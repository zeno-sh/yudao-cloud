package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/1/31
 */
@Data
public class PostingsDTO {
    @JSONField(name = "delivery_schema")
    private String deliverySchema;

    @JSONField(name = "order_date")
    private String orderDate;

    @JSONField(name = "posting_number")
    private String postingNumber;

    @JSONField(name = "warehouse_id")
    private Long warehouseId;

    // Getters and Setters ...
}
