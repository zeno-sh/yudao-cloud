package cn.iocoder.yudao.module.dm.dal.dataobject.transaction;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 交易记录 DO
 *
 * @author Zeno
 */
@TableName("dm_ozon_finance_transaction")
@KeySequence("dm_ozon_finance_transaction_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonFinanceTransactionDO extends TenantBaseDO {

    /**
     * 自动递增主键
     */
    @TableId
    private Long id;
    /**
     * 门店id
     */
    private String clientId;
    /**
     * 操作ID
     */
    private Long operationId;
    /**
     * 发货单号，orders和returns时有值
     */
    private String postingNumber;
    /**
     * 操作类型
     */
    private String operationType;
    /**
     * 操作日期
     */
    private LocalDate operationDate;
    /**
     * 操作类型名称
     */
    private String operationTypeName;
    /**
     * 配送费用（超大件）
     */
    private BigDecimal deliveryCharge;
    /**
     * 退货配送费用（超大件）
     */
    private BigDecimal returnDeliveryCharge;
    /**
     * 销售应计金额
     */
    private BigDecimal accrualsForSale;
    /**
     * 销售佣金或返还
     */
    private BigDecimal saleCommission;
    /**
     * 交易总金额
     */
    private BigDecimal amount;
    /**
     * 收费类型
     */
    private String type;
    /**
     * 发货信息（JSON格式）
     */
    private String posting;
    /**
     * 商品信息（JSON格式）
     */
    private String items;
    /**
     * 服务信息（JSON格式）
     */
    private String services;

}