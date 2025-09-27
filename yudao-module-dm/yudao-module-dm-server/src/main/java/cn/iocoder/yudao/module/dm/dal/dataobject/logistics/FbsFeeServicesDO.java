package cn.iocoder.yudao.module.dm.dal.dataobject.logistics;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 收费项目 DO
 *
 * @author Zeno
 */
@TableName("dm_fbs_fee_services")
@KeySequence("dm_fbs_fee_services_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbsFeeServicesDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 海外仓ID
     */
    private Long warehouseId;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 项目标签
     */
    private Integer tag;

}