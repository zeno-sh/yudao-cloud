package cn.iocoder.yudao.module.dm.controller.admin.product.vo;

import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductCustomsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPriceDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPlatformTrendDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.SupplierPriceOfferDO;

@Schema(description = "管理后台 - 产品信息新增/修改 Request VO")
@Data
public class ProductInfoSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "19133")
    private Long id;

    @Schema(description = "skuId", requiredMode = Schema.RequiredMode.REQUIRED, example = "1558")
    @NotEmpty(message = "skuId不能为空")
    private String skuId;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "产品名称不能为空")
    private String skuName;

    @Schema(description = "规格说明")
    private String specification;

    @Schema(description = "合并的标志", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "合并的标志不能为空")
    private String modelNumber;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "售卖状态", example = "2")
    private Integer saleStatus;

    @Schema(description = "类目ID", example = "18649")
    private Long categoryId;

    @Schema(description = "品牌ID", example = "9982")
    private Long brandId;

    @Schema(description = "标签", example = "4299")
    private Integer flagId;

    @Schema(description = "图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
//    @NotEmpty(message = "图片不能为空")
    private String pictureUrl;

    @Schema(description = "预估成本价", example = "11247")
    private BigDecimal costPrice;

    @Schema(description = "产品描述", example = "你猜")
    private String description;

    @Schema(description = "类目佣金")
    private BigDecimal categoryCommission;

    @Schema(description = "类目佣金Id", example = "28811")
    private Long categoryCommissionId;

    @Schema(description = "目标平台")
    private Integer platform;

    @Schema(description = "海关信息")
    private ProductCustomsDO productCustoms;

    @Schema(description = "产品价格策略列表")
    private List<ProductPriceDO> productPrices;

    @Schema(description = "产品销量趋势列表")
    private List<ProductPlatformTrendDO> productPlatformTrends;

    @Schema(description = "采购信息列表")
    private List<ProductPurchaseDO> productPurchases;

    @Schema(description = "供应商报价列表")
    private List<SupplierPriceOfferDO> supplierPriceOffers;

    @Schema(description = "产品成本结构")
    private List<ProductCostsDO> productCosts;
}