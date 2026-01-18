package cn.iocoder.yudao.module.chrome.dal.dataobject.order;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 订阅订单 DO
 *
 * @author Jax
 */
@TableName("chrome_subscription_order")
@KeySequence("chrome_subscription_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionOrderDO extends BaseDO {

    /**
     * 订单ID
     */
    @TableId
    private Long id;
    /**
     * 订单号
     */
    private String orderNo;
    /**
     * 支付订单ID（pay模块）
     */
    private Long payOrderId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 套餐ID
     */
    private Long planId;
    /**
     * 订阅类型
     */
    private Integer subscriptionType;
    /**
     * 计费周期
     */
    private Integer billingCycle;
    /**
     * 积分数量
     */
    private Integer credits;
    /**
     * 原价
     */
    private BigDecimal originalPrice;
    /**
     * 实付金额
     */
    private BigDecimal actualPrice;
    /**
     * 货币单位
     */
    private String currency;
    /**
     * 支付方式 （10微信 20支付宝 30其他）
     */
    private Integer paymentMethod;
    /**
     * 支付状态（10待支付 20已支付 30已取消 40已退款）
     */
    private Integer paymentStatus;
    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    /**
     * 订阅时长（天数）
     */
    private Integer durationDays;
    /**
     * 备注（如：推广赠送、首次订阅、续费等）
     */
    private String remark;

}