package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author: Zeno
 * @createTime: 2024/06/30 22:12
 */
@Data
public class ProductVolumeVO implements Serializable {
    private static final long serialVersionUID = -7139410004119309013L;

    /**
     * 订单量
     */
    private Integer todayOrderVolume;
    private Integer yesterdayOrderVolume;
    /**
     * 产品销量
     */
    private Integer todayProductVolume;
    private Integer yesterdayProductVolume;
    /**
     * 销售额
     */
    private BigDecimal todayAmount;
    private BigDecimal yesterdayAmount;
    /**
     * 取消订单量
     */
    private Integer todayCancelOrderVolume;
    private Integer yesterdayCancelOrderVolume;

    /**
     * 平均售价
     */
    private BigDecimal todayAvgPrice;
    private BigDecimal yesterdayAvgPrice;
}
