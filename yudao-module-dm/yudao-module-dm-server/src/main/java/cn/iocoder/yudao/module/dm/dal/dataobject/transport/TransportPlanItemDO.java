package cn.iocoder.yudao.module.dm.dal.dataobject.transport;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 头程计划明细 DO
 *
 * @author Zeno
 */
@TableName("dm_transport_plan_item")
@KeySequence("dm_transport_plan_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportPlanItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 头程计划ID
     */
    private Long planId;
    /**
     * 产品Id
     */
    private Long productId;
    /**
     * 采购单详情ID
     */
    private Long purchaseOrderItemId;
    /**
     * 发运数量
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
     * 上架状态
     *
     * 枚举 {@link TODO dm_first_choice 对应的类}
     */
    private String shelfStatus;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 海外仓入库单号
     */
    private String overseaLocationCheckinId;

}