package cn.iocoder.yudao.module.dm.api.dto;

import lombok.Data;

import java.util.List;

/**
 * 初始化汇率请求 DTO
 *
 * @author Zeno
 */
@Data
public class InitExchangeRatesReqDTO {

    /**
     * 租户编号
     */
    private Long tenantId;

    /**
     * 需要初始化的币种代码列表
     */
    private List<String> currencyCodes;

}
