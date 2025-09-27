package cn.iocoder.yudao.module.dm.dal.dataobject.ad;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 广告活动 DO
 *
 * @author Zeno
 */
@TableName("dm_ozon_ad_campaigns")
@KeySequence("dm_ozon_ad_campaigns_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonAdCampaignsDO extends TenantBaseDO {

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
     * 活动ID
     */
    private String campaignId;
    /**
     * 标题
     */
    private String title;
    /**
     * 展示量
     */
    private Integer views;
    /**
     * 点击数
     */
    private Integer clicks;
    /**
     * 广告花费
     */
    private BigDecimal moneySpent;
    /**
     * 平均报价
     */
    private BigDecimal avgBid;
    /**
     * 订单数量
     */
    private Integer orders;
    /**
     * 订单金额
     */
    private BigDecimal ordersMoney;
    /**
     * 日期
     */
    private LocalDate date;

}