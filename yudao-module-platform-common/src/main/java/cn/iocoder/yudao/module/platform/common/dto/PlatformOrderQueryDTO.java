package cn.iocoder.yudao.module.platform.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台订单查询条件 DTO
 *
 * @author Jax
 */
@Data
@Accessors(chain = true)
@Schema(description = "平台订单查询条件")
public class PlatformOrderQueryDTO implements Serializable {

    private static final long serialVersionUID = -5535540618859429224L;

    @Schema(description = "店铺ID列表", example = "[\"123456\"]")
    private List<String> shopIds;

    @Schema(description = "站点ID列表", example = "[\"ATVPDKIKX0DER\"]")
    private List<String> marketplaceIds;

    @Schema(description = "本地产品ID列表（用于筛选包含指定产品的订单）")
    private List<Long> localProductIds;

    @Schema(description = "履约类型：1=FBA 2=FBM，为空则查询全部", example = "1")
    private Integer fulfillmentType;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "订单状态列表", example = "[\"Shipped\"]")
    private List<String> orderStatuses;

}
