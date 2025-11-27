package cn.iocoder.yudao.module.platform.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 店铺统计数据查询条件 DTO
 *
 * @author Jax
 */
@Data
@Accessors(chain = true)
@Schema(description = "店铺统计数据查询条件")
public class ShopStatisticsQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺ID列表", example = "[\"123456\"]")
    private List<String> shopIds;

    @Schema(description = "站点ID列表", example = "[\"ATVPDKIKX0DER\"]")
    private List<String> marketplaceIds;

    @Schema(description = "开始日期", example = "2024-01-01")
    private String startDate;

    @Schema(description = "结束日期", example = "2024-01-31")
    private String endDate;

    @Schema(description = "币种", example = "USD")
    private String currency;

    @Schema(description = "是否倒序", example = "true")
    private Boolean desc;
}
