package cn.iocoder.yudao.module.dm.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门店映射信息DTO
 * 
 * @author Jax
 * @createTime: 2025/01/16 10:00
 */
@Data
public class ShopMappingDTO {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 平台类型
     * 枚举值对应 dm_platform 表
     */
    private Integer platform;

    /**
     * 门店名称
     */
    private String shopName;

    /**
     * 平台门店ID（客户端ID）
     */
    private String clientId;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 备用API密钥
     */
    private String apiKey2;

    /**
     * 广告客户端ID
     */
    private String adClientId;

    /**
     * 广告客户端密钥
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

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 更新者
     */
    private String updater;
} 