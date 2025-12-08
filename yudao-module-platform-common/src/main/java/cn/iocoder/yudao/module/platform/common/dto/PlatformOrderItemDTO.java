package cn.iocoder.yudao.module.platform.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 平台订单商品明细 DTO
 *
 * @author Jax
 */
@Data
@Accessors(chain = true)
@Schema(description = "平台订单商品明细")
public class PlatformOrderItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "平台商品SKU（如亚马逊的SellerSku）", example = "ABC-123")
    private String platformSku;

    @Schema(description = "ASIN（亚马逊专用，其他平台可为空）", example = "B09K48ZQWE")
    private String asin;

    @Schema(description = "本地产品ID（DM系统的产品ID）")
    private Long localProductId;

    @Schema(description = "购买数量", example = "2")
    private Integer quantity;

    @Schema(description = "商品单价", example = "49.99")
    private BigDecimal itemPrice;

    @Schema(description = "商品图片URL")
    private String imageUrl;

    @Schema(description = "商品标题")
    private String title;

}
