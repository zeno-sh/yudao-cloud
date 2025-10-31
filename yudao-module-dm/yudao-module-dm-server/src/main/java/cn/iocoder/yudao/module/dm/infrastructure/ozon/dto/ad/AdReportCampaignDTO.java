package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
@Data
public class AdReportCampaignDTO {

    /** 推广活动标题 */
    @JSONField(name = "title")
    private String title;

    /** 报告详情 */
    @JSONField(name = "report")
    private AdReportDataDTO report;
}
