package cn.iocoder.yudao.module.chrome.dal.dataobject.plan;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 订阅套餐配置 DO
 *
 * @author Jax
 */
@TableName("chrome_subscription_plan")
@KeySequence("chrome_subscription_plan_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDO extends BaseDO {

    /**
     * 套餐ID
     */
    @TableId
    private Long id;
    /**
     * 套餐名称
     */
    private String planName;
    /**
     * 平台类型（Amazon, Coupang等）
     */
    private String platform;
    /**
     * 订阅类型（10免费版 20基础版 30高级版）
     */
    private Integer subscriptionType;
    /**
     * 计费周期（10月付 20年付）
     */
    private Integer billingCycle;
    /**
     * 积分数量
     */
    private Integer credits;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 折扣率（百分比）
     */
    private BigDecimal discountRate;
    /**
     * 折扣后价格
     */
    private BigDecimal discountedPrice;
    /**
     * 货币单位
     */
    private String currency;
    /**
     * 状态（1启用 0禁用）
     */
    private Boolean status;
    /**
     * 排序
     */
    private Integer sortOrder;
    /**
     * 套餐描述
     */
    private String description;

}