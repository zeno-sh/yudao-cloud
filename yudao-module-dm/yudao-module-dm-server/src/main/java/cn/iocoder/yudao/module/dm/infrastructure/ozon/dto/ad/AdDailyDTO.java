package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
@Data
public class AdDailyDTO {
    @JSONField(name = "rows")
    private List<AdDailyItemDTO> rows;
}
