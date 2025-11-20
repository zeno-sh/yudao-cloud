package cn.iocoder.yudao.module.system.api.country.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 国家信息 Response DTO
 *
 * @author Zeno
 */
@Data
public class CountryRespDTO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 国家
     */
    private String country;

    /**
     * 地区
     */
    private String region;

    /**
     * 地区名称
     */
    private String regionName;

    /**
     * 币种
     */
    private String currencyCode;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

} 