package cn.iocoder.yudao.module.dm.controller.admin.productcosts.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 产品成本结构导入 Excel VO
 *
 * @author Jax
 */
@Schema(description = "产品成本结构导入")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = false)
@ExcelIgnoreUnannotated
public class ProductCostsExcelVO {

    @ExcelProperty(value = "目标平台", converter = DictConvert.class)
    @DictFormat("dm_platform")
    @ExcelColumnSelect(dictType = "dm_platform")
    private Integer platform;

    @ExcelProperty("Sku")
    private String skuId;

    @ExcelProperty(value = "采购成本币种", converter = DictConvert.class)
    @DictFormat("dm_currency_code")
    @ExcelColumnSelect(dictType = "dm_currency_code")
    private Integer purchaseCurrency;

    @ExcelProperty("采购成本")
    private BigDecimal purchaseCost;

    @ExcelProperty("采购运费")
    private BigDecimal purchaseShippingCost;

    @ExcelProperty(value = "采购运费单位", converter = DictConvert.class)
    @DictFormat("dm_fbs_unit_type")
    @ExcelColumnSelect(dictType = "dm_fbs_unit_type")
    private Integer purchaseShippingUnit;

    @ExcelProperty(value = "头程成本币种", converter = DictConvert.class)
    @DictFormat("dm_currency_code")
    @ExcelColumnSelect(dictType = "dm_currency_code")
    private Integer logisticsCurrency;

    @ExcelProperty("头程运费")
    private BigDecimal logisticsShippingCost;

    @ExcelProperty(value = "头程运费计费单位", converter = DictConvert.class)
    @DictFormat("dm_fbs_unit_type")
    @ExcelColumnSelect(dictType = "dm_fbs_unit_type")
    private Integer logisticsUnit;

    @ExcelProperty(value = "海关申报价币种", converter = DictConvert.class)
    @DictFormat("dm_currency_code")
    @ExcelColumnSelect(dictType = "dm_currency_code")
    private Integer customsCurrency;

    @ExcelProperty("海关关税费率")
    private BigDecimal customsDuty;

    @ExcelProperty("进口增值税")
    private BigDecimal importVat;

    @ExcelProperty("海关申报价")
    private BigDecimal customsDeclaredValue;

    @ExcelProperty(value = "FBO币种", converter = DictConvert.class)
    @DictFormat("dm_currency_code")
    @ExcelColumnSelect(dictType = "dm_currency_code")
    private Integer fboCurrency;

    @ExcelProperty("FBO送仓费")
    private BigDecimal fboDeliveryCost;

    @ExcelProperty(value = "FBO送仓费计费单位", converter = DictConvert.class)
    @DictFormat("dm_fbs_unit_type")
    @ExcelColumnSelect(dictType = "dm_fbs_unit_type")
    private Integer fboDeliveryCostUnit;

    @ExcelProperty("FBO验收费")
    private BigDecimal fboInspectionCost;

    @ExcelProperty(value = "FBO验收费计费单位", converter = DictConvert.class)
    @DictFormat("dm_fbs_unit_type")
    @ExcelColumnSelect(dictType = "dm_fbs_unit_type")
    private Integer fboInspectionCostUnit;

    @ExcelProperty("海外仓")
    private String fbsWarehouseIds;

    @ExcelProperty(value = "海外仓币种", converter = DictConvert.class)
    @DictFormat("dm_currency_code")
    @ExcelColumnSelect(dictType = "dm_currency_code")
    private Integer fbsCurrency;

    @ExcelProperty(value = "海外仓单位", converter = DictConvert.class)
    @DictFormat("dm_fbs_unit_type")
    @ExcelColumnSelect(dictType = "dm_fbs_unit_type")
    private Integer fbsCostUnit;

    @ExcelProperty("海外仓成本")
    private BigDecimal fbsCost;

    @ExcelProperty(value = "平台成本币种", converter = DictConvert.class)
    @DictFormat("dm_currency_code")
    @ExcelColumnSelect(dictType = "dm_currency_code")
    private Integer platformCurrency;

    @ExcelProperty("类目佣金ID")
    private Long categoryCommissionId;

    @ExcelProperty("转运费用")
    private BigDecimal platformLogisticsTransferCost;

    @ExcelProperty("最后一公里费用")
    private BigDecimal platformLogisticsLastMileCost;

    @ExcelProperty("收单费率")
    private BigDecimal orderFeeRate;

    @ExcelProperty("广告费率")
    private BigDecimal adFeeRate;

    @ExcelProperty("货损率")
    private BigDecimal damageRate;

    @ExcelProperty("销售税率")
    private BigDecimal salesTaxRate;

    private Long productId;
}