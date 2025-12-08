package cn.iocoder.yudao.module.platform.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 平台订单 DTO
 * <p>
 * 统一的多平台订单数据结构，各平台（Amazon、Coupang、Ozon等）必须返回此格式
 * </p>
 *
 * @author Jax
 */
@Data
@Accessors(chain = true)
@Schema(description = "平台订单数据")
public class PlatformOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 基础信息 ====================

    @Schema(description = "平台ID", example = "10")
    private Integer platformId;

    @Schema(description = "店铺ID", example = "123456")
    private String shopId;

    @Schema(description = "平台订单ID", example = "111-1234567-1234567")
    private String platformOrderId;

    @Schema(description = "站点ID", example = "ATVPDKIKX0DER")
    private String marketplaceId;

    // ==================== 履约信息 ====================

    @Schema(description = "履约类型：1=FBA(平台履约) 2=FBM(卖家自发货)", example = "1")
    private Integer fulfillmentType;

    @Schema(description = "订单状态", example = "Shipped")
    private String orderStatus;

    // ==================== 金额信息 ====================

    @Schema(description = "订单金额", example = "99.99")
    private BigDecimal orderAmount;

    @Schema(description = "币种", example = "USD")
    private String currency;

    // ==================== 时间信息 ====================

    @Schema(description = "下单时间")
    private LocalDateTime purchaseDate;

    @Schema(description = "收件人邮编", example = "10001")
    private String postalCode;

    // ==================== 订单明细 ====================

    @Schema(description = "订单商品明细列表")
    private List<PlatformOrderItemDTO> items;

}
