package cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo;

import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 海外仓产品库存 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FbsProductStockRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2654")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "29752")
    @ExcelProperty("仓库ID")
    private Long warehouseId;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "29752")
    @ExcelProperty("仓库名称")
    private String warehouseName;

    @Schema(description = "仓库Sku", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("仓库Sku")
    private String productSku;

    @Schema(description = "本地产品ID", example = "11463")
    @ExcelProperty("本地产品ID")
    private Long productId;

    @Schema(description = "在途数量")
    @ExcelProperty("在途数量")
    private Integer onway;

    @Schema(description = "可售数量")
    @ExcelProperty("可售数量")
    private Integer sellable;

    @Schema(description = "历史出库数量")
    @ExcelProperty("历史出库数量")
    private Integer shipped;

    @Schema(description = "同步时间")
    @ExcelProperty("同步时间")
    private LocalDateTime updateTime;

    @Schema(description = "产品信息")
    private ProductSimpleInfoVO productSimpleInfo;

}