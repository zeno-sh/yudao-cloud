package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 发货计划报表 Response VO")
@Data
public class TransportReportVO {

    @Schema(description = "月份，格式为：yyyy-MM", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-12")
    private String reportDate;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long productId;

    @Schema(description = "产品信息")
    private ProductSimpleInfoVO productSimpleInfoVO;

    @Schema(description = "期初在途数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer beginTransitQuantity;

    @Schema(description = "本期在途数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer currentTransitQuantity;

    @Schema(description = "本期到货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer currentArrivalQuantity;

    @Schema(description = "期末在途数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
    private Integer endTransitQuantity;

    @Schema(description = "在途货值(不含税)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000.00")
    private BigDecimal totalPrice;

    @Schema(description = "在途货值(含税)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1100.00")
    private BigDecimal totalTaxPrice;
} 