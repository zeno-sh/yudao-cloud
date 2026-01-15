package cn.iocoder.yudao.module.chrome.dal.dataobject.subscription;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 插件订阅 DO
 *
 * @author Jax
 */
@TableName("chrome_subscription")
@KeySequence("chrome_subscription_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDO extends BaseDO {

    /**
     * 订阅ID
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 订阅类型 10免费版 20基础版 30高级版
     */
    private Integer subscriptionType;
    /**
     * 状态（1有效 0无效）
     */
    private Boolean status;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 付费时长（天数）
     */
    private Integer paymentDuration;
    /**
     * 是否自动续费
     */
    private Boolean autoRenew;
    /**
     * 积分
     */
    private Integer credits;
    /**
     * 套餐ID
     */
    private Long planId;
    /**
     * 计费周期 10月付 20年付
     */
    private Integer billingCycle;
}