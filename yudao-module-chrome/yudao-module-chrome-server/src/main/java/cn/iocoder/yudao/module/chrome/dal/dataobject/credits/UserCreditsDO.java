package cn.iocoder.yudao.module.chrome.dal.dataobject.credits;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 用户积分账户 DO
 *
 * @author Jax
 */
@TableName("chrome_user_credits")
@KeySequence("chrome_user_credits_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreditsDO extends BaseDO {

    /**
     * 账户ID
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 总积分
     */
    private Integer totalCredits;
    /**
     * 已使用积分
     */
    private Integer usedCredits;
    /**
     * 剩余积分
     */
    private Integer remainingCredits;
    /**
     * 上次重置时间
     */
    private LocalDateTime lastResetTime;

}