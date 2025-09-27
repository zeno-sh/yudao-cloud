package cn.iocoder.yudao.module.dm.dal.dataobject.warehouse;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 海外仓平台仓映射 DO
 *
 * @author Zeno
 */
@TableName("dm_fbs_warehouse_mapping")
@KeySequence("dm_fbs_warehouse_mapping_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbsWarehouseMappingDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联的仓库ID
     */
    private Long warehouseId;
    /**
     * 门店ID
     */
    private String clientId;
    /**
     * 平台仓库ID
     */
    private String platformWarehouseId;
    /**
     * 平台仓库名称
     */
    private String platformWarehouseName;
    /**
     * 货物类型 10:普通 20:KGT
     */
    private Integer cargoType;

}