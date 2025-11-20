package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "利润计算详情 VO")
@Data
public class ProfitCalculationDetailVO {
    
    // 基础信息
    @Schema(description = "主键")
    private Long id;
    
    @Schema(description = "本地产品ID")
    private Long productId;
    
    @Schema(description = "选品计划名称")
    private String planName;
    
    @Schema(description = "平台")
    private Integer platform;
    
    @Schema(description = "国家")
    private String country;
    
    @Schema(description = "配置模板ID")
    private Long templateId;
    
    // 产品基础信息
    @Schema(description = "产品SKU")
    private String sku;
    
    @Schema(description = "产品长度(cm)")
    private BigDecimal productLength;
    
    @Schema(description = "产品宽度(cm)")
    private BigDecimal productWidth;
    
    @Schema(description = "产品高度(cm)")
    private BigDecimal productHeight;
    
    @Schema(description = "产品重量(kg)")
    private BigDecimal productWeight;
    
    // 售价和汇率
    @Schema(description = "售价")
    private BigDecimal price;
    
    @Schema(description = "币种")
    private Integer currency;
    
    @Schema(description = "汇率")
    private BigDecimal exchangeRate;
    
    // 成本输入
    @Schema(description = "采购单价（CNY，不含税）")
    private BigDecimal purchaseUnit;
    
    @Schema(description = "采购成本（目标币种，根据汇率计算）")
    private BigDecimal purchaseCost;
    
    @Schema(description = "配送费")
    private BigDecimal deliveryCost;
    
    @Schema(description = "仓储费")
    private BigDecimal storageCost;
    
    // 成本明细
    @Schema(description = "成本明细")
    private ProfitCalculationCostDetailVO costDetail;
    
    // 利润指标
    @Schema(description = "总成本")
    private BigDecimal totalCost;
    
    @Schema(description = "毛利润")
    private BigDecimal grossProfit;
    
    @Schema(description = "毛利率(%)")
    private BigDecimal grossMargin;
    
    @Schema(description = "投资回报率(%)")
    private BigDecimal roi;
    
    @Schema(description = "净利润")
    private BigDecimal netProfit;
    
    // 配置快照
    @Schema(description = "配置快照")
    private String configSnapshot;
    
    // 系统字段
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}