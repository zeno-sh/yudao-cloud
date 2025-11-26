package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "利润计算输入 VO")
@Data
public class ProfitCalculationInputVO {
    
    private Long productId;
    private String planName;
    private Integer platform;
    private String country;
    private Long templateId; // 可选，不填则使用默认模板
    
    // 产品基础信息
    private String sku;
    private BigDecimal productLength;
    private BigDecimal productWidth; 
    private BigDecimal productHeight;
    private BigDecimal productWeight;
    
    // 售价和汇率
    private BigDecimal price;
    private Integer currency;
    private BigDecimal exchangeRate;
    
    // 成本信息
    private BigDecimal purchaseUnit;
    private BigDecimal purchaseCost;
    private BigDecimal deliveryCost;
    private BigDecimal storageCost;
    private BigDecimal fbsCost;
}