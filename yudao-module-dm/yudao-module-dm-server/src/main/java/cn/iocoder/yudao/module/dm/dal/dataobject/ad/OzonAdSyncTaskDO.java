package cn.iocoder.yudao.module.dm.dal.dataobject.ad;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ozon广告同步任务 DO
 *
 * @author Jax
 */
@TableName(value="dm_ozon_ad_sync_task", autoResultMap = true)
@KeySequence("dm_ozon_ad_sync_task_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonAdSyncTaskDO extends TenantBaseDO {

    /**
     * 任务ID
     */
    @TableId
    private Long id;

    /**
     * 店铺ID
     */
    private String clientId;

    /**
     * 开始日期
     */
    private LocalDate beginDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 任务状态
     * 
     * 枚举 {@link cn.iocoder.yudao.module.dm.enums.OzonAdSyncTaskStatusEnum}
     */
    private Integer status;

    /**
     * 报告UUID
     */
    private String reportUuid;

    /**
     * 广告活动ID列表（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> campaignIds;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 备注
     */
    private String remark;

}