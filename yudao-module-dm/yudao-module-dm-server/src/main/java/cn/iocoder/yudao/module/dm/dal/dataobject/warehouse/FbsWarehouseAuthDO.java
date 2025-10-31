package cn.iocoder.yudao.module.dm.dal.dataobject.warehouse;

import lombok.*;

import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 海外仓授权信息 DO
 *
 * @author Zeno
 */
@TableName("dm_fbs_warehouse_auth")
@KeySequence("dm_fbs_warehouse_auth_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbsWarehouseAuthDO extends BaseDO {

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
     * 第三方海外仓
     * <p>
     * 枚举 {@link TODO dm_fbs_company 对应的类}
     */
    private Integer company;
    /**
     * Token值
     */
    private String token;
    /**
     * Shop ID
     */
    private String shopId;
    /**
     * API Key
     */
    private String apiKey;
    /**
     * Secret
     */
    private String secret;

}