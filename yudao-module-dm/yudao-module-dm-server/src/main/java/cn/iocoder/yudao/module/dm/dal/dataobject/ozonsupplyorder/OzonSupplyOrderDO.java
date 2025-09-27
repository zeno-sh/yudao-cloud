package cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder;

import lombok.*;

import java.time.*;
import java.util.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 供应订单 DO
 *
 * @author Zeno
 */
@TableName("dm_ozon_supply_order")
@KeySequence("dm_ozon_supply_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonSupplyOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * Ozon客户端ID
     */
    private String clientId;
    /**
     * Ozon供应订单ID
     */
    private Long supplyOrderId;
    /**
     * Ozon供应订单编号
     */
    private String supplyOrderNumber;
    /**
     * 创建日期
     */
    private LocalDate creationDate;
    /**
     * 订单状态
     */
    private String state;
    /**
     * 仓库ID
     */
    private Long warehouseId;
    /**
     * 仓库名称
     */
    private String warehouseName;
    /**
     * 配送时间段开始
     */
    private LocalDateTime timeslotFrom;
    /**
     * 配送时间段结束
     */
    private LocalDateTime timeslotTo;
    /**
     * 商品总数
     */
    private Integer totalItems;
    /**
     * 总体积(升)
     */
    private BigDecimal totalVolume;
    /**
     * 手动编辑
     */
    private Boolean updatedManually;

}