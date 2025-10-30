package cn.iocoder.yudao.module.dm.controller.admin.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 组合产品明细 Response VO")
@Data
public class ProductBundleItemRespVO {
    
    @Schema(description = "关系ID", example = "1")
    private Long id;
    
    @Schema(description = "子产品ID", example = "5")
    private Long subProductId;
    
    // ========== 以下字段从 dm_product_info 表 JOIN 获取（实时数据）==========
    
    @Schema(description = "子产品SKU（实时）", example = "210-Gray")
    private String subSkuId;
    
    @Schema(description = "子产品名称（实时）", example = "牛排机")
    private String subSkuName;
    
    @Schema(description = "单位（实时）", example = "个")
    private String unit;
    
    @Schema(description = "单位成本价（实时）", example = "118.00")
    private BigDecimal unitCostPrice;
    
    // ========== 以下字段从关系表获取 ==========
    
    @Schema(description = "数量", example = "2")
    private Integer quantity;
    
    @Schema(description = "总成本价（计算：unitCostPrice * quantity）", example = "236.00")
    private BigDecimal totalCostPrice;
    
    @Schema(description = "排序", example = "1")
    private Integer sortOrder;
    
    @Schema(description = "备注")
    private String remark;
}

