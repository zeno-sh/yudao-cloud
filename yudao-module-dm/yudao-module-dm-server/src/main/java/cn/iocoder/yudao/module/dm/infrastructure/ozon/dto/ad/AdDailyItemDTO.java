package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.CustomBigDecimalReader;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
@Data
public class AdDailyItemDTO {

    /** 模板ID */
    @JSONField(name = "id")
    private String campaignId;

    /** 活动标题 */
    @JSONField(name = "title")
    private String title;

    /** 日期 */
    @JSONField(name = "date")
    private String date;

    /** 浏览次数 */
    @JSONField(name = "views")
    private String views;

    /** 点击次数 */
    @JSONField(name = "clicks")
    private String clicks;

    /** 花费的金额 */
    @JSONField(deserializeUsing = CustomBigDecimalReader.class)
    private String moneySpent;

    /** 平均出价 */
    @JSONField(deserializeUsing = CustomBigDecimalReader.class)
    private String avgBid;

    /** 订单数量 */
    @JSONField(name = "orders", deserializeUsing = CustomBigDecimalReader.class)
    private String orders;

    /** 订单金额 */
    @JSONField(deserializeUsing = CustomBigDecimalReader.class)
    private String ordersMoney;
}
