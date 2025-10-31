package cn.iocoder.yudao.module.dm.dal.dataobject.warehouse;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 海外仓仓库 DO
 *
 * @author Zeno
 */
@TableName("dm_fbs_warehouse")
@KeySequence("dm_fbs_warehouse_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbsWarehouseDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 仓库名称
     */
    private String name;
    /**
     * 城市
     */
    private String city;
    /**
     * 地址
     */
    private String address;
    /**
     * 联系人
     */
    private String contactPerson;
    /**
     * 电话
     */
    private String phone;
    /**
     * 体积重系数
     */
    private Integer volumetricWeightFactor;
    /**
     * 备注
     */
    private String remark;

}