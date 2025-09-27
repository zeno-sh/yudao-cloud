package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ad;

import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.CustomBigDecimalReader;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
@Data
public class AdReportSkuDTO {
    /** 浏览次数 */
    @JSONField(name = "views")
    private String views;

    /** 点击次数 */
    @JSONField(name = "clicks")
    private String clicks;

    /** 点击率 */
    @JSONField(name = "ctr", deserializeUsing = CustomBigDecimalReader.class)
    private String ctr;

    /** 转化率 */
    @JSONField(name = "cr", deserializeUsing = CustomBigDecimalReader.class)
    private String cr;

    /** 花费金额 */
    @JSONField(name = "moneySpent", deserializeUsing = CustomBigDecimalReader.class)
    private String moneySpent;

    /** 平均出价 */
    @JSONField(name = "avgBid", deserializeUsing = CustomBigDecimalReader.class)
    private String avgBid;

    /** 订单数量 */
    @JSONField(name = "orders", deserializeUsing = CustomBigDecimalReader.class)
    private String orders;

    /** 订单金额 */
    @JSONField(name = "ordersMoney", deserializeUsing = CustomBigDecimalReader.class)
    private String ordersMoney;

    /** 商品模型数量 */
    @JSONField(name = "models", deserializeUsing = CustomBigDecimalReader.class)
    private String models;

    /** 商品模型金额 */
    @JSONField(name = "modelsMoney", deserializeUsing = CustomBigDecimalReader.class)
    private String modelsMoney;

    /** 商品SKU编号 */
    @JSONField(name = "sku")
    private String sku;

    /** 商品标题 */
    @JSONField(name = "title")
    private String title;

    /** 商品价格 */
    @JSONField(name = "price", deserializeUsing = CustomBigDecimalReader.class)
    private String price;

    /**
     * 搜索广告时有值
     */
    @JSONField(name = "orderId")
    private String orderId;

    /**
     * 订单金额，搜索广告时有值
     */
    @JSONField(name = "cost", deserializeUsing = CustomBigDecimalReader.class)
    private String cost;

    /**
     * 平均报价，搜索广告时有值
     */
    @JSONField(name = "bidValue", deserializeUsing = CustomBigDecimalReader.class)
    private String bidValue;

    private String date;
}
