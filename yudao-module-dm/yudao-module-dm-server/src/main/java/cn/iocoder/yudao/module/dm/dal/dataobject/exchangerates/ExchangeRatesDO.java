package cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 汇率 DO
 *
 * @author Zeno
 */
@TableName("dm_exchange_rates")
@KeySequence("dm_exchange_rates_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRatesDO extends TenantBaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 官方汇率
     */
    private BigDecimal officialRate;
    /**
     * 自定义汇率
     */
    private BigDecimal customRate;
    @Deprecated
    private Integer baseCurrency;
    @Deprecated
    private Integer targetCurrency;

    /**
     * 货币代码
     * 与表 system_currency 对齐
     *
     */
    private String currencyCode;

}