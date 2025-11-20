package cn.iocoder.yudao.module.dm.controller.admin.productcosts.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 产品成本结构新增/修改 Request VO")
@Data
public class ProductCostsSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3255")
    private Long id;

    @Schema(description = "目标平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标平台不能为空")
    private Integer platform;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20754")
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @Schema(description = "采购成本币种")
    @Deprecated
    private Integer purchaseCurrency;

    @Schema(description = "采购成本币种代码")
    private String purchaseCurrencyCode;

    @Schema(description = "采购运费")
    private BigDecimal purchaseShippingCost;

    @Schema(description = "采购运费计费单位")
    private Integer purchaseShippingUnit;

    @Schema(description = "头程成本币种")
    @Deprecated
    private Integer logisticsCurrency;

    @Schema(description = "头程成本币种代码")
    private String logisticsCurrencyCode;

    @Schema(description = "头程运费")
    private BigDecimal logisticsShippingCost;

    @Schema(description = "头程运费计费单位")
    private Integer logisticsUnit;

    @Schema(description = "海关申报价币种")
    @Deprecated
    private Integer customsCurrency;

    @Schema(description = "海关申报价币种代码")
    private String customsCurrencyCode;

    @Schema(description = "海关关税费率")
    private BigDecimal customsDuty;

    @Schema(description = "进口增值税")
    private BigDecimal importVat;

    @Schema(description = "海关申报价")
    private BigDecimal customsDeclaredValue;

    @Schema(description = "FBO送仓费")
    private BigDecimal fboDeliveryCost;

    @Schema(description = "FBO送仓费计费单位")
    private Integer fboDeliveryCostUnit;

    @Schema(description = "FBO验收费")
    private BigDecimal fboInspectionCost;

    @Schema(description = "FBO验收费计费单位")
    private Integer fboInspectionCostUnit;

    @Schema(description = "FBO仓储费")
    private BigDecimal fboStorageCost;

    @Schema(description = "货损率")
    private BigDecimal damageRate;

    @Schema(description = "销售税率")
    private BigDecimal salesTaxRate;

    @Schema(description = "银行提现费率")
    private BigDecimal bankWithdrawRate;

    @Schema(description = "类目佣金ID", example = "7821")
    private Long categoryCommissionId;

    @Schema(description = "收单费率")
    private BigDecimal orderFeeRate;

    @Schema(description = "采购成本")
    private BigDecimal purchaseCost;

    @Schema(description = "最后一公里费用")
    private BigDecimal platformLogisticsLastMileCost;

    @Schema(description = "转运费用")
    private BigDecimal platformLogisticsTransferCost;

    @Schema(description = "广告费率")
    private BigDecimal adFeeRate;

    @Schema(description = "FBO送仓费币种")
    @Deprecated
    private Integer fboCurrency;

    @Schema(description = "FBO送仓费币种代码")
    private String fboCurrencyCode;

    @Schema(description = "平台成本币种")
    @Deprecated
    private Integer platformCurrency;

    @Schema(description = "平台成本币种代码")
    private String platformCurrencyCode;

    @Schema(description = "海外仓")
    private List<Long> fbsWarehouseIds;

    @Schema(description = "海外仓币种")
    @Deprecated
    private Integer fbsCurrency;

    @Schema(description = "海外仓币种代码")
    private String fbsCurrencyCode;

    @Schema(description = "海外仓单位")
    private Integer fbsCostUnit;

    @Schema(description = "海外仓成本")
    private BigDecimal fbsCost;

}