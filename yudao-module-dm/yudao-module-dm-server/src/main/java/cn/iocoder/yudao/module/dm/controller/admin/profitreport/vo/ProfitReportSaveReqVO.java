package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 财务账单报告新增/修改 Request VO")
@Data
public class ProfitReportSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "16314")
    private Integer id;

    @Schema(description = "账单日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "账单日期不能为空")
    private LocalDate financeDate;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7216")
    @NotEmpty(message = "门店ID不能为空")
    private String clientId;

    @Schema(description = "本地产品ID", example = "32666")
    private Long productId;

    @Schema(description = "平台货号", example = "28710")
    private String offerId;

    @Schema(description = "平台skuId", example = "28710")
    private String platformSkuId;

    @Schema(description = "订单数量")
    private Integer orders;

    @Schema(description = "产品销量")
    private Integer salesVolume;

    @Schema(description = "平台币种")
    private Integer platformCurrency;

    @Schema(description = "销售金额")
    private BigDecimal salesAmount;

    @Schema(description = "结算金额")
    private BigDecimal settleAmount;

    @Schema(description = "结算金额")
    private BigDecimal refundSettleAmount;

    @Schema(description = "佣金")
    private BigDecimal categoryCommissionCost;

    @Schema(description = "退还佣金")
    private BigDecimal returnCommissionAmount;

    @Schema(description = "退货/取消 金额")
    private BigDecimal cancelledAmount;

    @Schema(description = "逆向物流")
    private BigDecimal reverseLogisticsCost;

    @Schema(description = "收单")
    private BigDecimal orderFeeCost;

    @Schema(description = "头程币种")
    private Integer logisticsCurrency;

    @Schema(description = "头程运费")
    private BigDecimal logisticsShippingCost;

    @Schema(description = "最后一公里")
    private BigDecimal logisticsLastMileCost;

    @Schema(description = "转运费")
    private BigDecimal logisticsTransferCost;

    @Schema(description = "drop-off")
    private BigDecimal logisticsDropOff;

    @Schema(description = "其他代理服务费")
    private BigDecimal otherAgentServiceCost;

    @Schema(description = "退货数量")
    private Integer refundOrders;

    @Schema(description = "退款/赔偿")
    private BigDecimal refundAmount;

    @Schema(description = "平台服务费")
    private BigDecimal platformServiceCost;

    @Schema(description = "FBO送仓费")
    private BigDecimal fboDeliverCost;

    @Schema(description = "FBO验收费")
    private BigDecimal fboInspectionCost;

    @Schema(description = "FBS币种")
    private Integer fbsCurrency;

    @Schema(description = "FBS操作费")
    private BigDecimal fbsCheckInCost;

    @Schema(description = "FBS操作费")
    private BigDecimal fbsOperatingCost;

    @Schema(description = "FBS其他费用")
    private BigDecimal fbsOtherCost;

    @Schema(description = "销售VAT")
    private BigDecimal salesVatCost;

    @Schema(description = "进口VAT")
    private BigDecimal vatCost;

    @Schema(description = "关税金额")
    private BigDecimal customsCost;

    @Schema(description = "采购成本币种")
    private Integer purchaseCurrency;

    @Schema(description = "采购价")
    private BigDecimal purchaseCost;

    @Schema(description = "采购运费")
    private BigDecimal purchaseShippingCost;

    @Schema(description = "清关货值")
    private BigDecimal declaredValueCost;

    @Schema(description = "海关申报币种")
    private Integer customsCurrency;

    @Schema(description = "利润")
    private BigDecimal profitAmount;

    @Schema(description = "租户Id")
    private Long tenantId;

}