package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
@Data
public class AdReportTotalDTO {
    /** 总浏览次数 */
    @JSONField(name = "views")
    private String views;

    /** 总点击次数 */
    @JSONField(name = "clicks")
    private String clicks;

    /** 总点击率 */
    @JSONField(name = "ctr")
    private String ctr;

    /** 总转化率 */
    @JSONField(name = "cr")
    private String cr;

    /** 总花费金额 */
    @JSONField(name = "moneySpent")
    private String moneySpent;

    /** 总平均出价 */
    @JSONField(name = "avgBid")
    private String avgBid;

    /** 总订单数量 */
    @JSONField(name = "orders")
    private String orders;

    /** 总订单金额 */
    @JSONField(name = "ordersMoney")
    private String ordersMoney;

    /** 总商品模型数量 */
    @JSONField(name = "models")
    private String models;

    /** 总商品模型金额 */
    @JSONField(name = "modelsMoney")
    private String modelsMoney;

    /** 校正项 */
    @JSONField(name = "corrections")
    private String corrections;
}
