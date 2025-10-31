package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
@Data
public class AdReportDataDTO {
    /** 行数据列表 */
    @JSONField(name = "rows")
    private List<AdReportSkuDTO> rows;

    /** 总计数据 */
    @JSONField(name = "totals")
    private AdReportTotalDTO totals;
}
