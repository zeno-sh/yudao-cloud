package cn.iocoder.yudao.module.chrome.dal.dataobject.user;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 用户 DO
 *
 * @author Jax
 */
@TableName("chrome_user")
@KeySequence("chrome_user_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDO extends BaseDO {

    /**
     * 用户ID
     */
    @TableId
    private Long id;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 密码（加密）
     */
    private String password;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 状态（1正常 0禁用）
     */
    private Boolean status;
    /**
     * 最后登录IP
     */
    private String loginIp;
    /**
     * 最后登录时间
     */
    private LocalDateTime loginDate;
    /**
     * 设备令牌（用于单设备登录）
     */
    private String deviceToken;

    /**
     * 我的推广码
     */
    private String referralCode;

    /**
     * 推荐人用户ID
     */
    private Long referrerUserId;

}