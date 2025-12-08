package cn.iocoder.yudao.module.platform.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 平台库存 DTO
 * <p>
 * 统一的多平台库存数据结构，各平台（Amazon、Coupang、Ozon等）必须返回此格式
 * </p>
 *
 * @author Jax
 */
@Data
@Accessors(chain = true)
@Schema(description = "平台库存数据")
public class PlatformInventoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 基础信息 ====================

    @Schema(description = "平台ID", example = "10")
    private Integer platformId;

    @Schema(description = "店铺ID", example = "123456")
    private String shopId;

    @Schema(description = "站点ID", example = "ATVPDKIKX0DER")
    private String marketplaceId;

    @Schema(description = "本地产品ID（DM系统的产品ID）")
    private Long localProductId;

    // ==================== SKU信息 ====================

    @Schema(description = "平台商品SKU", example = "ABC-123")
    private String platformSku;

    @Schema(description = "ASIN（亚马逊专用）", example = "B09K48ZQWE")
    private String asin;

    @Schema(description = "FNSKU（亚马逊FBA专用）", example = "X001234567")
    private String fnsku;

    // ==================== 履约信息 ====================

    @Schema(description = "履约类型：1=FBA(平台履约) 2=FBM(卖家自发货)", example = "1")
    private Integer fulfillmentType;

    // ==================== 库存数量 ====================

    @Schema(description = "可售库存", example = "100")
    private Integer availableQty;

    @Schema(description = "在途库存（入库中）", example = "50")
    private Integer inboundQty;

    @Schema(description = "总库存", example = "150")
    private Integer totalQty;

    @Schema(description = "预留库存（待发货等）", example = "10")
    private Integer reservedQty;

    @Schema(description = "不可售库存", example = "5")
    private Integer unfulfillableQty;

}
