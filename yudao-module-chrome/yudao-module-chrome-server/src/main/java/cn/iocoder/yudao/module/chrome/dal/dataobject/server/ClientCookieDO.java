package cn.iocoder.yudao.module.chrome.dal.dataobject.server;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * Chrome 插件cookie配置 DO
 *
 * @author Jax
 */
@TableName("chrome_client_cookie")
@KeySequence("chrome_client_cookie_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCookieDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 服务器id
     */
    private Integer serverId;
    /**
     * 用户key
     */
    private String uuid;
    /**
     * 密码
     */
    private String password;
    /**
     * cookie
     */
    private String cookie;
    /**
     * 类型 10=前台,20=后台
     */
    private Integer type;
    /**
     * WebSocket会话ID
     */
    private String wsSessionId;
    /**
     * WebSocket上报的账号
     */
    private String wsAccount;
    /**
     * WebSocket客户端IP地址
     */
    private String wsIpAddress;
    /**
     * WebSocket连接时间
     */
    private LocalDateTime wsConnectTime;
    /**
     * WebSocket最后心跳时间
     */
    private LocalDateTime wsLastHeartbeatTime;
    /**
     * WebSocket在线状态(0=离线,1=在线)
     */
    private Boolean wsOnline;
    /**
     * WebSocket上报的状态数据(JSON格式)
     */
    private String wsStatusData;

}