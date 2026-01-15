package cn.iocoder.yudao.module.chrome.dal.dataobject.email;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * Chrome邮箱验证码 DO
 *
 * @author Jax
 */
@TableName("chrome_email_code")
@KeySequence("chrome_email_code_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailCodeDO extends BaseDO {

    /**
     * 验证码ID
     */
    @TableId
    private Long id;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 验证码
     */
    private String code;
    /**
     * 场景（10注册 20忘记密码 30修改密码）
     */
    private Integer scene;
    /**
     * 是否已使用
     */
    private Boolean used;
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

}