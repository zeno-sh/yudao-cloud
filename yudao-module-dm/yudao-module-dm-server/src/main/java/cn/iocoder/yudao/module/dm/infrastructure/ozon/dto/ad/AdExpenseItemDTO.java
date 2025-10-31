package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.CustomBigDecimalReader;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
@Data
public class AdExpenseItemDTO {

    /**
     * 模板ID
     */
    @JSONField(name = "id")
    private String campaignId;
    /**
     * 广告日期
     */
    private String date;
    /**
     * 目标标题
     */
    private String title;
    /**
     * 广告花费
     */
    @JSONField(deserializeUsing = CustomBigDecimalReader.class)
    private String moneySpent;
}
