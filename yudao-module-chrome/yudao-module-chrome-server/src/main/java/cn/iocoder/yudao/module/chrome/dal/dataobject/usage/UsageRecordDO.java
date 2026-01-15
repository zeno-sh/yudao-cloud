package cn.iocoder.yudao.module.chrome.dal.dataobject.usage;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * Chrome使用记录 DO
 *
 * @author Jax
 */
@TableName("chrome_usage_record")
@KeySequence("chrome_usage_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRecordDO extends BaseDO {

    /**
     * 记录ID
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 功能类型
     */
    private Integer featureType;
    /**
     * 使用日期
     */
    private LocalDate usageDate;
    /**
     * 使用次数
     */
    private Integer usageCount;
    /**
     * 消费积分数
     */
    private Integer creditsConsumed;
    /**
     * 卖家商品ID
     */
    private String sellerProductId;
}