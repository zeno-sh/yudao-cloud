package cn.iocoder.yudao.module.dm.dal.dataobject.transport;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 头程计划 DO
 *
 * @author Zeno
 */
@TableName(value= "dm_transport_plan", autoResultMap = true)
@KeySequence("dm_transport_plan_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportPlanDO extends BaseDO {

    /**
     * 发货计划
     */
    private String code;
    /**
     * 运输状态
     */
    private Integer transportStatus;
    /**
     * 海外仓入库单号
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> overseaLocationCheckinId;
    /**
     * 货代公司
     */
    private String forwarder;
    /**
     * 报价
     */
    private BigDecimal offerPrice;
    /**
     * 币种
     */
    private Integer currency;
    /**
     * 结算状态
     */
    private String settleStatus;
    /**
     * 账单（人民币）
     */
    private BigDecimal billPrice;
    /**
     * 备注
     */
    private String remark;
    /**
     * 发运日期
     */
    private LocalDateTime despatchDate;
    /**
     * 预计抵达日期
     */
    private LocalDateTime arrivalDate;
    /**
     * 实际抵达日期
     */
    private LocalDateTime finishedDate;
    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 文件数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;

}