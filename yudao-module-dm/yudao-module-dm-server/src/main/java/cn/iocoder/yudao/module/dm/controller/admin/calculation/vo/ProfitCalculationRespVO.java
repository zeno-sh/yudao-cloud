package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 利润预测 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProfitCalculationRespVO {

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("序号")
    private Integer rowNum;

    @Schema(description = "选品计划名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("选品计划名称")
    private String planName;

    @Schema(description = "国家", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("国家")
    private String country;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "平台", converter = DictConvert.class)
    @DictFormat("dm_platform")
    private Integer platform;

    @Schema(description = "币种代码")
    @ExcelProperty("币种代码")
    private String currencyCode;

    @Schema(description = "汇率")
    @ExcelProperty("汇率")
    private BigDecimal exchangeRate;

    @Schema(description = "本地产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "31633")
    @ExcelProperty("本地产品ID")
    private Long productId;

    @Schema(description = "产品SKU")
    @ExcelProperty("产品SKU")
    private String sku;

    @Schema(description = "产品名称")
    @ExcelProperty("产品名称")
    private String skuName;

    @Schema(description = "毛利润")
    @ExcelProperty("毛利润")
    private BigDecimal grossProfit;

    @Schema(description = "毛利率(%)")
    @ExcelProperty("毛利率(%)")
    private BigDecimal grossMargin;

    @Schema(description = "投资回报率(%)")
    @ExcelProperty("投资回报率(%)")
    private BigDecimal roi;

    @Schema(description = "净利润")
    @ExcelProperty("净利润")
    private BigDecimal netProfit;

    @Schema(description = "配置模板ID")
    @ExcelProperty("配置模板ID")
    private Long templateId;

    // 基础产品信息
    @Schema(description = "产品长度(cm)")
    @ExcelProperty("产品长度(cm)")
    private BigDecimal productLength;

    @Schema(description = "产品宽度(cm)")
    @ExcelProperty("产品宽度(cm)")
    private BigDecimal productWidth;

    @Schema(description = "产品高度(cm)")
    @ExcelProperty("产品高度(cm)")
    private BigDecimal productHeight;

    @Schema(description = "产品重量(kg)")
    @ExcelProperty("产品重量(kg)")
    private BigDecimal productWeight;

    // 计算中间结果
    @Schema(description = "立方米")
    @ExcelProperty("立方米")
    private BigDecimal volumeM3;

    @Schema(description = "计算重量")
    @ExcelProperty("计算重量")
    private BigDecimal calculateWeight;

    @Schema(description = "计算体积")
    @ExcelProperty("计算体积")
    private BigDecimal calculateVolume;

    @Schema(description = "实际体积")
    @ExcelProperty("实际体积")
    private BigDecimal actualVolume;

    @Schema(description = "实际重量")
    @ExcelProperty("实际重量")
    private BigDecimal actualWeight;
    
    // 计算配置参数
    @Schema(description = "体积系数（立方米转重量）")
    @ExcelProperty("体积系数（立方米转重量）")
    private BigDecimal volumeCoefficient;
    
    @Schema(description = "重量系数（重量转体积）")
    @ExcelProperty("重量系数（重量转体积）")
    private BigDecimal weightCoefficient;
    
    @Schema(description = "国内运费单价（每立方米）")
    @ExcelProperty("国内运费单价（每立方米）")
    private BigDecimal domesticFreightUnit;
    
    @Schema(description = "货代费用单价（每立方米）")
    @ExcelProperty("货代费用单价（每立方米）")
    private BigDecimal freightForwarderUnit;
    
    @Schema(description = "关税率(%)")
    @ExcelProperty("关税率(%)")
    private BigDecimal tariffRate;
    
    @Schema(description = "VAT税率(%)")
    @ExcelProperty("VAT税率(%)")
    private BigDecimal vatRate;
    
    @Schema(description = "申报比例(%)")
    @ExcelProperty("申报比例(%)")
    private BigDecimal declarationRatio;
    
    @Schema(description = "类目佣金率(%)")
    @ExcelProperty("类目佣金率(%)")
    private BigDecimal categoryCommissionRate;
    
    @Schema(description = "数字服务费率(%)")
    @ExcelProperty("数字服务费率(%)")
    private BigDecimal digitalServiceRate;
    
    @Schema(description = "是否启用FBA：1-是，0-否")
    @ExcelProperty("是否启用FBA：1-是，0-否")
    private Integer fbaEnabled;
    
    @Schema(description = "广告费率(%)")
    @ExcelProperty("广告费率(%)")
    private BigDecimal adRate;
    
    @Schema(description = "退货率(%)")
    @ExcelProperty("退货率(%)")
    private BigDecimal returnRate;
    
    @Schema(description = "海运计费方式：1-按体积，2-按重量")
    @ExcelProperty("海运计费方式：1-按体积，2-按重量")
    private Integer shippingCalculationType;
    
    @Schema(description = "海运单价")
    @ExcelProperty("海运单价")
    private BigDecimal shippingUnitPrice;

    // 售价和汇率
    @Schema(description = "售价", requiredMode = Schema.RequiredMode.REQUIRED, example = "25053")
    @ExcelProperty("售价")
    private BigDecimal price;

    // 成本输入
    @Schema(description = "采购单价（CNY，不含税）")
    @ExcelProperty("采购单价（CNY，不含税）")
    private BigDecimal purchaseUnit;
    
    @Schema(description = "采购成本（目标币种，根据汇率计算）")
    @ExcelProperty("采购成本（目标币种，根据汇率计算）")
    private BigDecimal purchaseCost;

    @Schema(description = "配送费")
    @ExcelProperty("配送费")
    private BigDecimal deliveryCost;

    @Schema(description = "仓储费")
    @ExcelProperty("仓储费")
    private BigDecimal storageCost;

    // 各项费用明细
    @Schema(description = "国内运费")
    @ExcelProperty("国内运费")
    private BigDecimal localTransportCost;

    @Schema(description = "货代费用")
    @ExcelProperty("货代费用")
    private BigDecimal freightForwarderCost;

    @Schema(description = "关税费用")
    @ExcelProperty("关税费用")
    private BigDecimal tariffCost;

    @Schema(description = "VAT费用")
    @ExcelProperty("VAT费用")
    private BigDecimal vatCost;

    @Schema(description = "头程运费")
    @ExcelProperty("头程运费")
    private BigDecimal firstMileFreightCost;

    @Schema(description = "类目佣金费用")
    @ExcelProperty("类目佣金费用")
    private BigDecimal categoryCommissionCost;

    @Schema(description = "数字服务费用")
    @ExcelProperty("数字服务费用")
    private BigDecimal digitalServiceCost;

    @Schema(description = "广告费用")
    @ExcelProperty("广告费用")
    private BigDecimal adCost;

    @Schema(description = "退换货费用")
    @ExcelProperty("退换货费用")
    private BigDecimal returnCost;

    // 利润计算结果
    @Schema(description = "总成本")
    @ExcelProperty("总成本")
    private BigDecimal totalCost;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}