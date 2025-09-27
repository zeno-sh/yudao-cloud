package cn.iocoder.yudao.module.dm.dal.dataobject.commission;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 类目佣金 DO
 *
 * @author Zeno
 */
@TableName("dm_category_commission")
@KeySequence("dm_category_commission_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCommissionDO extends BaseDO {

    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 类目名称
     */
    private String categoryName;
    /**
     * 类目佣金
     */
    private BigDecimal rate;
    /**
     * 平台
     */
    private Long parentId;

}