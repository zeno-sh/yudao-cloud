package cn.iocoder.yudao.module.dm.controller.admin.report.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * SKU报表响应 VO
 *
 * @author Jax
 */
@Schema(description = "管理后台 - SKU报表响应 VO")
@Data
public class SkuReportRespVO {
    
    // ========== 基础信息 ==========
    
    @Schema(description = "产品ID", example = "1")
    @ExcelProperty("产品ID")
    private Long productId;
    
    @Schema(description = "产品简单信息")
    private ProductSimpleInfoVO productSimpleInfo;
    
    @Schema(description = "SKU", example = "ABC-111")
    @ExcelProperty("SKU")
    private String sku;
    
    @Schema(description = "各个平台的关联信息", example = "B09K48ZQWE")
    @ExcelProperty("平台sku")
    private String platformSkuId;
    
    // ========== FBA库存 ==========
    
    @Schema(description = "FBA可用库存", example = "400")
    @ExcelProperty("FBA")
    private Integer fbaAvailableQty;
    
    @Schema(description = "FBA在途库存", example = "20")
    @ExcelProperty("FBA在途库存")
    private Integer fbaInboundQty;
    
    // ========== 海外仓库存 ==========
    
    @Schema(description = "各仓库库存 Map<仓库代码, 库存数量>", example = "{\"US-SOUTH\": 100, \"US-WEST\": 100, \"US-EAST\": 100}")
    private Map<String, Integer> warehouseInventoryMap;
    
    @Schema(description = "各仓库在途库存 Map<仓库代码, 在途数量>", example = "{\"US-SOUTH\": 50, \"US-WEST\": 30}")
    private Map<String, Integer> warehouseInboundMap;
    
    // ========== 统计信息 ==========
    
    @Schema(description = "海外仓总库存", example = "400")
    @ExcelProperty("总库存")
    private Integer overseasTotalQty;
    
    @Schema(description = "海外仓在途总库存", example = "80")
    @ExcelProperty("在途总库存")
    private Integer overseasInboundQty;
    
    @Schema(description = "总库存(海外仓 + FBA)", example = "800")
    @ExcelProperty("库存可销售天")
    private Integer totalQty;
    
    @Schema(description = "库存可销售天数(最大显示90)", example = "134")
    @ExcelProperty("库存可销售天数")
    private Integer availableDays;
    
    @Schema(description = "在途可销售天数", example = "20")
    @ExcelProperty("在途可销售天数")
    private Integer inboundAvailableDays;
    
    @Schema(description = "统计天数", example = "7")
    @ExcelProperty("统计天数")
    private Integer days;
    
    // ========== 销量统计 ==========
    
    @Schema(description = "各平台日均销量统计 Map<平台ID, 日均销量>", example = "{1: 15.5, 2: 20.8}")
    private Map<Integer, Double> platformSalesMap;
    
    @Schema(description = "平台合计日销量(各平台日销量之和除以平台数量)", example = "18.15")
    @ExcelProperty("平台合计日销量")
    private Double platformAvgDailySales;
    
    @Schema(description = "各仓库日均销量统计 Map<仓库代码, 日均销量>", example = "{\"US-SOUTH\": 10.5, \"US-WEST\": 5.2}")
    private Map<String, Double> warehouseSalesMap;
}
