package cn.iocoder.yudao.module.system.api.currency.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 汇率信息 Response DTO
 *
 * @author Jax
 */
@Data
public class ExchangeRateRespDTO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 基础货币代码(关联currency表)
     */
    private String baseCurrency;

    /**
     * 1单位基础货币=多少人民币
     */
    private BigDecimal rate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}