package cn.iocoder.yudao.module.dm.dal.dataobject.warehouse;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 海外仓推单记录 DO
 *
 * @author Zeno
 */
@TableName("dm_fbs_push_order_log")
@KeySequence("dm_fbs_push_order_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbsPushOrderLogDO extends BaseDO {

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
     * 本地订单ID
     */
    private Long orderId;
    /**
     * 本地订单ID
     */
    private String platformOrderId;
    /**
     * 发货编号
     */
    private String postingNumber;
    /**
     * 请求参数
     */
    private String request;
    /**
     * 响应结果
     */
    private String response;
    /**
     * 状态
     */
    private Boolean status;

}