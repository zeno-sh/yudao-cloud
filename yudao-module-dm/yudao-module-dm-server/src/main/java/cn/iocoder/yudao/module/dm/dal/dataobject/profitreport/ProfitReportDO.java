package cn.iocoder.yudao.module.dm.dal.dataobject.profitreport;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 财务账单报告 DO
 *
 * @author Zeno
 */
@TableName("dm_profit_report")
@KeySequence("dm_profit_report_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitReportDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 账单日期
     */
    private LocalDate financeDate;
    /**
     * 门店ID
     */
    private String clientId;
    /**
     * 本地产品ID
     */
    private Long productId;
    /**
     * 平台货号
     */
//    @Deprecated
    private String offerId;
    /**
     * 平台skuId
     */
    private String platformSkuId;
    /**
     * 订单数量
     */
    private Integer orders;
    /**
     * 产品销量
     */
    private Integer salesVolume;
    /**
     * 销售金额
     */
    private BigDecimal salesAmount;
    /**
     * 结算金额
     */
    private BigDecimal settleAmount;
    /**
     * 佣金
     */
    private BigDecimal categoryCommissionCost;
    /**
     * 退还佣金
     */
    private BigDecimal returnCommissionAmount;
    /**
     * 退货/取消 金额
     */
    private BigDecimal cancelledAmount;
    /**
     * 逆向物流
     */
    private BigDecimal reverseLogisticsCost;
    /**
     * 收单
     */
    private BigDecimal orderFeeCost;
    /**
     * 头程运费
     */
    private BigDecimal logisticsShippingCost;
    /**
     * 最后一公里
     */
    private BigDecimal logisticsLastMileCost;
    /**
     * 转运费
     */
    private BigDecimal logisticsTransferCost;
    /**
     * drop-off
     */
    private BigDecimal logisticsDropOff;
    /**
     * 其他代理服务费
     */
    private BigDecimal otherAgentServiceCost;
    /**
     * 退货数量
     */
    private Integer refundOrders;
    /**
     * 退货金额
     */
    private BigDecimal refundAmount;
    /**
     * 平台服务费
     */
    private BigDecimal platformServiceCost;
    /**
     * FBO送仓费
     */
    private BigDecimal fboDeliverCost;
    /**
     * FBO验收费
     */
    private BigDecimal fboInspectionCost;
    /**
     * FBS操作费
     */
    private BigDecimal fbsCheckInCost;
    /**
     * FBS操作费
     */
    private BigDecimal fbsOperatingCost;
    /**
     * FBS其他费用
     */
    private BigDecimal fbsOtherCost;
    /**
     * 销售VAT
     */
    private BigDecimal salesVatCost;
    /**
     * 进口VAT
     */
    private BigDecimal vatCost;
    /**
     * 关税金额
     */
    private BigDecimal customsCost;
    /**
     * 采购价
     */
    private BigDecimal purchaseCost;
    /**
     * 采购运费
     */
    private BigDecimal purchaseShippingCost;
    /**
     * 清关货值
     */
    private BigDecimal declaredValueCost;
    /**
     * 利润
     */
    private BigDecimal profitAmount;
    /**
     * 平台补偿金额
     */
    private BigDecimal compensationAmount;

    /**
     * 平台费用币种
     */
    private Integer platformCurrency;

    /**
     * FBS费用币种
     */
    private Integer fbsCurrency;

    /**
     * 采购成本币种
     */
    private Integer purchaseCurrency;

    /**
     * 头程币种
     */
    private Integer logisticsCurrency;

    /**
     * 海关申报币种
     */
    private Integer customsCurrency;

}