package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/1/31
 */
@Data
public class ItemDTO {
    private String name;

    @JSONField(name = "sku")
    private long sku;

    // Getters and Setters ...
}
