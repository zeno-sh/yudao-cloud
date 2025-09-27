package cn.iocoder.yudao.module.dm.dal.dataobject.logistics;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 收费明细 DO
 *
 * @author Zeno
 */
@TableName("dm_fbs_fee_detail")
@KeySequence("dm_fbs_fee_detail_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbsFeeDetailDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 收费项目ID
     */
    private Long serviceId;
    /**
     * 计费方式
     */
    private Integer pricingMethod;
    /**
     * 最小值
     */
    private BigDecimal min;
    /**
     * 最大值
     */
    private BigDecimal max;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 单位
     */
    private Integer unit;

}