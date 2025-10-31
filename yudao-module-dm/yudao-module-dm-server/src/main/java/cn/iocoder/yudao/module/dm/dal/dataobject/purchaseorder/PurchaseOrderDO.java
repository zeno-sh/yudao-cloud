package cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购单 DO
 *
 * @author Zeno
 */
@TableName("dm_purchase_order")
@KeySequence("dm_purchase_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 采购单号
     */
    private String orderNo;
    /**
     * 预付比例
     */
    private Integer prepmentRatio;
    /**
     * 供应商ID
     */
    private Long supplierId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 附件url
     */
    private String attachments;
    /**
     * 结算账期
     */
    private LocalDateTime settleDate;
    /**
     * 单据负责人
     */
    private Long owner;
    /**
     * 结算方式 10=现结 20=月结
     */
    private Integer settleType;
    /**
     * 是否含税 0=不含 1=含
     */
    private Boolean tax;
    /**
     * 运费
     */
    private BigDecimal transportationPrice;
    /**
     * 其他费用
     */
    private BigDecimal otherPrice;
    /**
     * 合计金额
     */
    private BigDecimal totalPrice;
    /**
     * 合计货值
     */
    private BigDecimal totalProductPrice;
    /**
     * 合计数量
     */
    private Integer totalCount;
    /**
     * 已付款金额
     */
    private BigDecimal paymentPrice;
    /**
     * 合计税额
     */
    private BigDecimal totalTaxPrice;
    /**
     * 采购状态
     */
    private Integer status;
    /**
     * 关联的采购计划批次编号
     */
    private String batchNumber;
    /**
     * 已到货数量
     */
    private Integer arriveQuantity;

}