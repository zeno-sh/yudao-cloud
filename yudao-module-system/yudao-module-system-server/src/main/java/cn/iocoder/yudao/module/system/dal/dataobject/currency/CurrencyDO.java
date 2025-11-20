package cn.iocoder.yudao.module.system.dal.dataobject.currency;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 币种信息 DO
 *
 * @author Jax
 */
@TableName("system_currency")
@KeySequence("system_currency_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    
    /**
     * ISO货币代码(3位字母)
     */
    private String currencyCode;
    
    /**
     * 货币名称
     */
    private String name;
    
    /**
     * 货币符号
     */
    private String symbol;

}