package cn.iocoder.yudao.module.dm.dal.dataobject.purchase;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购计划详情 DO
 *
 * @author Zeno
 */
@TableName("dm_purchase_plan_item")
@KeySequence("dm_purchase_plan_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanItemDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 计划编号
     */
    private String planNumber;
    /**
     * 计划id
     */
    private Long planId;
    /**
     * 产品Id
     */
    private Long productId;
    /**
     * skuId
     */
    private String skuId;
    /**
     * 采购数量
     */
    private Integer quantity;
    /**
     * pcs
     */
    private Integer pcs;
    /**
     * 箱数
     */
    private Integer numberOfBox;
    /**
     * 体积
     */
    private BigDecimal volume;
    /**
     * 重量
     */
    private BigDecimal weight;
    /**
     * 采购订单
     */
    private String purchaseOrder;
    /**
     * 备注
     */
    private String remark;
    /**
     * 采购状态
     *
     * 枚举 {@link TODO dm_purchase_plan_status 对应的类}
     */
    private Integer status;
    /**
     * 审核状态
     * 字典：bpm_task_status
     */
    private Integer auditStatus;
    /**
     * 期望到货时间
     */
    private LocalDateTime expectedArrivalDate;

}