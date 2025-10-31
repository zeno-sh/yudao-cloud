package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author: Zeno
 * @createTime: 2024/07/02 21:58
 */
@Data
public class ProductAdVO implements Serializable {
    private static final long serialVersionUID = -29069126316721645L;

    /**
     * 花费
     */
    private BigDecimal todaySpend;
    private BigDecimal yesterdaySpend;

    /**
     * 总销售额
     */
    private BigDecimal todayAmount;
    private BigDecimal yesterdayAmount;

    /**
     * 广告销售额
     */
    private Integer todayAdVolume;
    private Integer yesterdayAdVolume;

    /**
     * 广告花费/广告销售额
     */
    private BigDecimal todayAcos;
    private BigDecimal yesterdayAcos;

    /**
     * 广告花费/总销售额
     */
    private BigDecimal todayAcoas;
    private BigDecimal yesterdayAcoas;

    /**
     * 广告销售额/总销售额=广告销售占比
     */
    private BigDecimal todayAdRate;
    private BigDecimal yesterdayAdRate;
}
