package cn.iocoder.yudao.module.dm.dal.dataobject.ad;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import lombok.*;

import java.time.LocalDate;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

/**
 * 广告明细 DO
 *
 * @author Zeno
 */
@TableName("dm_ozon_ad_campaigns_item")
@KeySequence("dm_ozon_ad_campaigns_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonAdCampaignsItemDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 门店ID
     */
    private String clientId;
    /**
     * 平台Sku
     */
    private String platformSkuId;
    /**
     * 活动ID
     */
    private String campaignId;
    /**
     * 展示量
     */
    private Integer views;
    /**
     * 点击量
     */
    private Integer clicks;
    /**
     * cr
     */
    private BigDecimal cr;
    /**
     * 花费
     */
    private BigDecimal moneySpent;
    /**
     * 平均报价
     */
    private BigDecimal avgBid;
    /**
     * 订单量
     */
    private Integer orders;
    /**
     * 订单金额
     */
    private BigDecimal ordersMoney;
    /**
     * 产品售价
     */
    private BigDecimal price;
    /**
     * 平台订单ID，搜索广告时有值
     */
    private String orderId;
    /**
     * 日期
     */
    private LocalDate date;

}