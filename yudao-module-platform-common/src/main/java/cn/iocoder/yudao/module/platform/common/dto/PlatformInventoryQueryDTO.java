package cn.iocoder.yudao.module.platform.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 平台库存查询条件 DTO
 *
 * @author Jax
 */
@Data
@Accessors(chain = true)
@Schema(description = "平台库存查询条件")
public class PlatformInventoryQueryDTO implements Serializable {

    private static final long serialVersionUID = -4723506835256849362L;

    @Schema(description = "店铺ID列表", example = "[\"123456\"]")
    private List<String> shopIds;

    @Schema(description = "站点ID列表", example = "[\"ATVPDKIKX0DER\"]")
    private List<String> marketplaceIds;

    @Schema(description = "本地产品ID列表")
    private List<Long> localProductIds;

    @Schema(description = "履约类型：1=FBA 2=FBM，为空则查询全部", example = "1")
    private Integer fulfillmentType;

}
