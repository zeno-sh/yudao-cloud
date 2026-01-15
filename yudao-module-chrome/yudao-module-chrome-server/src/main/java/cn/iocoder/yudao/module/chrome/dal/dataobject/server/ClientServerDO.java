package cn.iocoder.yudao.module.chrome.dal.dataobject.server;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * Chrome 插件cookie服务器 DO
 *
 * @author Jax
 */
@TableName("chrome_client_server")
@KeySequence("chrome_client_server_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientServerDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 服务器ip
     */
    private String ip;
    /**
     * 端口
     */
    private Integer port;

}