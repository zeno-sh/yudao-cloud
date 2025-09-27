package cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ozon店铺 DO
 *
 * @author zeno
 */
@TableName("dm_ozon_shop_mapping")
@KeySequence("dm_ozon_shop_mapping_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OzonShopMappingDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 平台
     *
     * 枚举 {@link TODO dm_platform 对应的类}
     */
    private Integer platform;
    /**
     * 门店名称
     */
    private String shopName;
    /**
     * 平台门店Id
     */
    private String clientId;
    /**
     * 密钥
     */
    private String apiKey;
    /**
     * 备用API密钥
     */
    private String apiKey2;
    /**
     * 广告key
     */
    private String adClientId;
    /**
     * 广告密钥
     */
    private String adClientSecret;
    /**
     * API密钥过期时间
     */
    private LocalDateTime apiExpireTime;
    /**
     * 授权状态：10-正常 20-已过期 30-已禁用 40-待审核
     */
    private Integer authStatus;

}