package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/13
 */
@Data
public class IncludeDTO {

    @JSONField(name = "analytics_data")
    private boolean analyticsData;
    private boolean barcodes;
    @JSONField(name = "financial_data")
    private boolean financialData;
    private boolean translit;
}
