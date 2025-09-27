package cn.iocoder.yudao.module.dm.dal.dataobject.purchase;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 采购计划 DO
 *
 * @author Zeno
 */
@TableName("dm_purchase_plan")
@KeySequence("dm_purchase_plan_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanDO extends BaseDO {

    /**
     * 批次编号
     */
    private String batchNumber;
    /**
     * 备注
     */
    private String remark;
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * bpm审批流程Id
     */
    private String processInstanceId;
    /**
     * 审批状态
     *
     * 枚举 {@link TODO bpm_task_status 对应的类}
     */
    private Integer auditStatus;
    /**
     * 采购状态
     * 参见字典：dm_purchase_plan_status
     */
    private Integer status;

    /**
     * 合计数量
     */
    private Integer totalCount;

    /**
     * 部门编号
     */
    private Long deptId;
}