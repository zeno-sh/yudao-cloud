package cn.iocoder.yudao.module.dm.controller.admin.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductPurchaseDO;

@Schema(description = "管理后台 - 选品计划新增/修改 Request VO")
@Data
public class ProductSelectionPlanSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1592")
    private Long id;

    @Schema(description = "选品计划编号")
    private String planCode;

    @Schema(description = "选品计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String planName;

    @Schema(description = "SKU", requiredMode = Schema.RequiredMode.REQUIRED, example = "30509")
    private String planSkuId;

    @Schema(description = "价格计划", example = "10055")
    private Long priceId;

    @Schema(description = "供应商报价", example = "29694")
    private Long supplierPriceOfferId;

    @Schema(description = "货代报价（人民币）", example = "12688")
    private BigDecimal forwarderPrice;

    @Schema(description = "手动预估采购价", example = "2984")
    private BigDecimal forecastPurchasePrice;

    @Schema(description = "广告费率")
    private BigDecimal adRate;

    @Schema(description = "货损率")
    private BigDecimal lossRate;

    @Schema(description = "本地产品ID", example = "12075")
    private Long productId;

    @Schema(description = "采购信息列表")
    private List<ProductPurchaseDO> productPurchases;

}