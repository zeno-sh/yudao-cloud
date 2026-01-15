package cn.iocoder.yudao.module.chrome.controller.admin.collect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品采集响应 VO
 * 
 * @author Jax
 */
@Schema(description = "管理后台 - 商品采集 Response VO")
@Data
public class ProductCollectRespVO {

    @Schema(description = "商品ID", example = "B08N5WRWNW")
    private String productId;

    @Schema(description = "商品标题", example = "Apple iPhone 13 Pro Max")
    private String title;

    @Schema(description = "商品价格", example = "999.99")
    private BigDecimal price;

    @Schema(description = "货币单位", example = "USD")
    private String currency;

    @Schema(description = "商品图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "品牌", example = "Apple")
    private String brand;

    @Schema(description = "分类", example = "Electronics")
    private String category;

    @Schema(description = "评分", example = "4.5")
    private BigDecimal rating;

    @Schema(description = "评论数量", example = "1234")
    private Integer reviewCount;

    @Schema(description = "库存状态", example = "IN_STOCK")
    private String stockStatus;

    @Schema(description = "卖家信息", example = "Amazon")
    private String seller;

    @Schema(description = "采集时间")
    private LocalDateTime collectTime;

    @Schema(description = "平台类型", example = "AMAZON")
    private String platform;

    @Schema(description = "商品URL", example = "https://www.amazon.com/dp/B08N5WRWNW")
    private String productUrl;

}