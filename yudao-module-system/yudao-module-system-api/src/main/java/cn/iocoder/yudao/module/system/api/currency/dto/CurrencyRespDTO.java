package cn.iocoder.yudao.module.system.api.currency.dto;

import lombok.Data;

/**
 * 币种信息 Response DTO
 *
 * @author Jax
 */
@Data
public class CurrencyRespDTO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * ISO货币代码(3位字母)
     */
    private String currencyCode;

    /**
     * 货币名称
     */
    private String name;

    /**
     * 货币符号
     */
    private String symbol;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

}