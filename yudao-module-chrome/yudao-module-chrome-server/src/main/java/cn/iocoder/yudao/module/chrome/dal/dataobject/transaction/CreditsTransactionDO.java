package cn.iocoder.yudao.module.chrome.dal.dataobject.transaction;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 积分交易记录 DO
 *
 * @author Jax
 */
@TableName("chrome_credits_transaction")
@KeySequence("chrome_credits_transaction_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditsTransactionDO extends BaseDO {

    /**
     * 交易ID
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 交易类型（10充值 20消费 30赠送）
     */
    private Integer transactionType;
    /**
     * 积分数量
     */
    private Integer creditsAmount;
    /**
     * 交易前积分
     */
    private Integer beforeCredits;
    /**
     * 交易后积分
     */
    private Integer afterCredits;
    /**
     * 业务类型（消费时关联功能类型）
     */
    private Integer businessType;
    /**
     * 业务ID（订单ID或使用记录ID）
     */
    private String businessId;
    /**
     * 交易描述
     */
    private String description;

}