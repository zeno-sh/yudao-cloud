package cn.iocoder.yudao.module.dm.controller.admin.productcosts.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 产品成本结构 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductCostsRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3255")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "目标平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "目标平台", converter = DictConvert.class)
    @DictFormat("dm_platform") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer platform;

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20754")
    @ExcelProperty("产品ID")
    private Long productId;

    @Schema(description = "采购成本币种")
    @ExcelProperty(value = "采购成本币种", converter = DictConvert.class)
    @DictFormat("dm_currency_code") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer purchaseCurrency;

    @Schema(description = "采购运费")
    @ExcelProperty("采购运费")
    private BigDecimal purchaseShippingCost;

    @Schema(description = "采购运费计费单位")
    @ExcelProperty(value = "采购运费计费单位", converter = DictConvert.class)
    @DictFormat("dm_fbs_unit_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer purchaseShippingUnit;

    @Schema(description = "头程运费")
    @ExcelProperty("头程运费")
    private BigDecimal logisticsShippingCost;

    @Schema(description = "头程运费计费单位")
    @ExcelProperty("头程运费计费单位")
    private Integer logisticsUnit;

    @Schema(description = "海关关税费率")
    @ExcelProperty("海关关税费率")
    private BigDecimal customsDuty;

    @Schema(description = "进口增值税")
    @ExcelProperty("进口增值税")
    private BigDecimal importVat;

    @Schema(description = "海关申报价")
    @ExcelProperty("海关申报价")
    private BigDecimal customsDeclaredValue;

    @Schema(description = "FBO送仓费")
    @ExcelProperty("FBO送仓费")
    private BigDecimal fboDeliveryCost;

    @Schema(description = "FBO送仓费计费单位")
    @ExcelProperty("FBO送仓费计费单位")
    private Integer fboDeliveryCostUnit;

    @Schema(description = "FBO验收费")
    @ExcelProperty("FBO验收费")
    private BigDecimal fboInspectionCost;

    @Schema(description = "FBO验收费计费单位")
    @ExcelProperty("FBO验收费计费单位")
    private Integer fboInspectionCostUnit;

    @Schema(description = "货损率")
    @ExcelProperty("货损率")
    private BigDecimal damageRate;

    @Schema(description = "销售税率")
    @ExcelProperty("销售税率")
    private BigDecimal salesTaxRate;

    @Schema(description = "银行提现费率")
    @ExcelProperty("银行提现费率")
    private BigDecimal bankWithdrawRate;

    @Schema(description = "类目佣金ID", example = "7821")
    @ExcelProperty("类目佣金ID")
    private Long categoryCommissionId;

    @Schema(description = "收单费率")
    @ExcelProperty("收单费率")
    private BigDecimal orderFeeRate;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "采购成本")
    @ExcelProperty("采购成本")
    private BigDecimal purchaseCost;

    @Schema(description = "头程成本币种")
    @ExcelProperty("头程成本币种")
    private Integer logisticsCurrency;

    @Schema(description = "最后一公里费用")
    @ExcelProperty("最后一公里费用")
    private BigDecimal platformLogisticsLastMileCost;

    @Schema(description = "转运费用")
    @ExcelProperty("转运费用")
    private BigDecimal platformLogisticsTransferCost;

    @Schema(description = "广告费率")
    @ExcelProperty("广告费率")
    private BigDecimal adFeeRate;

    @Schema(description = "海关申报价币种")
    @ExcelProperty("海关申报价币种")
    private Integer customsCurrency;

    @Schema(description = "FBO送仓费币种")
    @ExcelProperty("FBO送仓费币种")
    private Integer fboCurrency;

    @Schema(description = "平台成本币种")
    @ExcelProperty("平台成本币种")
    private Integer platformCurrency;

    @Schema(description = "海外仓")
    @ExcelProperty("海外仓")
    private Collection<Long> fbsWarehouseIds;

    @Schema(description = "海外仓币种")
    @ExcelProperty(value = "海外仓币种", converter = DictConvert.class)
    @DictFormat("dm_currency_code")
    private Integer fbsCurrency;

    @Schema(description = "海外仓单位")
    @ExcelProperty(value = "海外仓单位", converter = DictConvert.class)
    @DictFormat("dm_fbs_unit_type")
    private Integer fbsCostUnit;

    @Schema(description = "海外仓成本")
    @ExcelProperty("海外仓成本")
    private BigDecimal fbsCost;

}