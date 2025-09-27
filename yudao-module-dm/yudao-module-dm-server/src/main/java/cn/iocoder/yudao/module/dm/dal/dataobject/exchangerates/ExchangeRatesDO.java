package cn.iocoder.yudao.module.dm.dal.dataobject.exchangerates;

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
public class ExchangeRatesDO extends BaseDO {

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
    /**
     * 基础货币代码（ISO标准）
     *
     * 枚举 {@link TODO dm_currency_code 对应的类}
     */
    private Integer baseCurrency;
    /**
     * 目标货币代码（ISO标准）
     *
     * 枚举 {@link TODO dm_currency_code 对应的类}
     */
    private Integer targetCurrency;

    /**
     * 货币代码
     *
     */
    private String currencyCode;

}