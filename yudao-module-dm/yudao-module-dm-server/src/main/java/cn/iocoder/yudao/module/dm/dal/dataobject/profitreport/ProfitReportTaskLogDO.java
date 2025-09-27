package cn.iocoder.yudao.module.dm.dal.dataobject.profitreport;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务账单报告计算任务日志 DO
 *
 * @author zeno
 */
@TableName("dm_profit_report_task_log")
@KeySequence("dm_profit_report_task_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProfitReportTaskLogDO extends BaseDO {

    /**
     * 日志ID
     */
    @TableId
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 门店ID数组，以逗号分隔
     */
    private String clientIds;

    /**
     * 查询维度：sku、client
     */
    private String dimension;

    /**
     * 时间类型：day、week、month
     */
    private String timeType;

    /**
     * 开始时间
     */
    private LocalDate startDate;

    /**
     * 结束时间
     */
    private LocalDate endDate;

    /**
     * 任务执行开始时间
     */
    private LocalDateTime executeStartTime;

    /**
     * 任务执行结束时间
     */
    private LocalDateTime executeEndTime;

    /**
     * 执行状态：0-执行中，1-执行成功，2-执行失败
     */
    private Integer status;

    /**
     * 错误信息，格式化存储执行过程中的错误
     */
    private String errorInfo;

    /**
     * 执行日志，记录执行过程
     */
    private String executeLog;

    /**
     * 影响记录数
     */
    private Integer affectedRecords;
} 