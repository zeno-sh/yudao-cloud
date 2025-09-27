package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2025/03/31 20:54
 */
@Data
public class ProductOnlineStatusDTO {

    @JSONField(name = "status_name")
    private String statusName;
}
