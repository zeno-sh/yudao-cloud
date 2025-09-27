package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo;

import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ClientSimpleInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 财务账单报告 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProfitReportRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "16314")
    @ExcelProperty("主键")
    private Integer id;

    @Schema(description = "账单日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("账单日期")
    private String financeDate;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7216")
    @ExcelProperty("门店ID")
    private String clientId;

    @Schema(description = "门店")
    private String shopName;

    @Schema(description = "平台")
    private Integer platform;

    @Schema(description = "平台名称")
    private String platformName;

    @Schema(description = "本地产品ID", example = "32666")
    @ExcelProperty("本地产品ID")
    private Long productId;

    @Schema(description = "平台货号", example = "28710")
    @ExcelProperty("平台货号")
    private String offerId;

    @Schema(description = "平台SKU ID", example = "1299050967")
    @ExcelProperty("平台SKU ID")
    private String platformSkuId;

    @Schema(description = "产品信息")
    private ProductSimpleInfoVO productSimpleInfo;

    @Schema(description = "订单数量")
    @ExcelProperty("订单数量")
    private Integer orders;

    @Schema(description = "产品销量")
    @ExcelProperty("产品销量")
    private Integer salesVolume;

    @Schema(description = "销售金额")
    @ExcelProperty("销售金额")
    private BigDecimal salesAmount;

    @Schema(description = "结算金额")
    @ExcelProperty("结算金额")
    private BigDecimal settleAmount;
    @Schema(description = "结算金额")

    @ExcelProperty("取消结算")
    private BigDecimal refundSettleAmount;

    @Schema(description = "退还佣金")
    @ExcelProperty("退还佣金")
    private BigDecimal returnCommissionAmount;

    @Schema(description = "退货/取消 金额")
    @ExcelProperty("退货/取消 金额")
    private BigDecimal cancelledAmount;

    @Schema(description = "逆向物流")
    @ExcelProperty("逆向物流")
    private BigDecimal reverseLogisticsCost;

    @Schema(description = "头程")
    @ExcelProperty("头程")
    private BigDecimal logisticsShippingCost;

    @Schema(description = "佣金")
    @ExcelProperty("佣金")
    private BigDecimal categoryCommissionCost;

    @Schema(description = "收单")
    @ExcelProperty("收单")
    private BigDecimal orderFeeCost;

    @Schema(description = "最后一公里")
    @ExcelProperty("最后一公里")
    private BigDecimal logisticsLastMileCost;

    @Schema(description = "转运费")
    @ExcelProperty("转运费")
    private BigDecimal logisticsTransferCost;

    @Schema(description = "drop-off")
    @ExcelProperty("drop-off")
    private BigDecimal logisticsDropOff;

    @Schema(description = "其他代理服务费")
    @ExcelProperty("其他代理服务费")
    private BigDecimal otherAgentServiceCost;

    @Schema(description = "退货数量")
    @ExcelProperty("退货数量")
    private Integer refundOrders;

    @Schema(description = "退款/赔偿 金额")
    @ExcelProperty("退款/赔偿 金额")
    private BigDecimal refundAmount;

    @Schema(description = "平台服务费")
    @ExcelProperty("平台服务费")
    private BigDecimal platformServiceCost;

    @Schema(description = "FBO送仓费")
    @ExcelProperty("FBO送仓费")
    private BigDecimal fboDeliverCost;

    @Schema(description = "FBO验收费")
    @ExcelProperty("FBO验收费")
    private BigDecimal fboInspectionCost;

    @Schema(description = "FBS入仓费")
    @ExcelProperty("FBS入仓费")
    private BigDecimal fbsCheckInCost;

    @Schema(description = "FBS操作费")
    @ExcelProperty("FBS操作费")
    private BigDecimal fbsOperatingCost;

    @Schema(description = "FBS其他费用")
    @ExcelProperty("FBS其他费用")
    private BigDecimal fbsOtherCost;

    @Schema(description = "销售VAT")
    @ExcelProperty("销售VAT")
    private BigDecimal salesVatCost;

    @Schema(description = "进口VAT")
    @ExcelProperty("进口VAT")
    private BigDecimal vatCost;

    @Schema(description = "关税金额")
    @ExcelProperty("关税金额")
    private BigDecimal customsCost;

    @Schema(description = "采购价")
    @ExcelProperty("采购价")
    private BigDecimal purchaseCost;

    @Schema(description = "采购运费")
    @ExcelProperty("采购运费")
    private BigDecimal purchaseShippingCost;

    @Schema(description = "清关货值")
    @ExcelProperty("清关货值")
    private BigDecimal declaredValueCost;

    @Schema(description = "毛利率")
    @ExcelProperty("毛利率")
    private BigDecimal profitRate;

    @Schema(description = "毛利润")
    @ExcelProperty("毛利润")
    private BigDecimal profitAmount;

    @Schema(description = "平台补偿金额")
    @ExcelProperty("平台补偿金额")
    private BigDecimal compensationAmount;

    @Schema(description = "roi=利润/(采购成本+采购运费+头程运费)")
    @ExcelProperty("roi")
    private BigDecimal roi;

    @Schema(description = "Acos")
    @ExcelProperty("Acos")
    private BigDecimal acos;

    @Schema(description = "acoas")
    @ExcelProperty("acoas")
    private BigDecimal acoas;

    @Schema(description = "广告销售额")
    @ExcelProperty("广告销售额")
    private BigDecimal adAmount;

    @Schema(description = "广告花费")
    @ExcelProperty("广告花费")
    private BigDecimal adCost;

    @Schema(description = "广告销量")
    @ExcelProperty("广告销量")
    private Integer adOrders;

    /**
     * 平台费用币种
     */
    @Schema(description = "平台费用币种")
    private Integer platformCurrency;

    /**
     * FBS费用币种
     */
    @Schema(description = "FBS费用币种")
    private Integer fbsCurrency;

    /**
     * 采购成本币种
     */
    @Schema(description = "采购成本币种")
    private Integer purchaseCurrency;

    /**
     * 头程币种
     */
    @Schema(description = "头程币种")
    private Integer logisticsCurrency;

    /**
     * 海关申报币种
     */
    @Schema(description = "海关申报币种")
    private Integer customsCurrency;
}