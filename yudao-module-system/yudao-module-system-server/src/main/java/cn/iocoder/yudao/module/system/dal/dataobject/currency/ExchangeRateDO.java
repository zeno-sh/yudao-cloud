package cn.iocoder.yudao.module.system.dal.dataobject.currency;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 汇率信息 DO
 *
 * @author Jax
 */
@TableName("system_exchange_rate")
@KeySequence("system_exchange_rate_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    
    /**
     * 基础货币代码(关联currency表)
     */
    private String baseCurrency;
    
    /**
     * 1单位基础货币=多少人民币
     */
    private BigDecimal rate;

}