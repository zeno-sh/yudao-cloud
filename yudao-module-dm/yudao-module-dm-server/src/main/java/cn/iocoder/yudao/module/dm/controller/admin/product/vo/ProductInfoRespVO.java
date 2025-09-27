package cn.iocoder.yudao.module.dm.controller.admin.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 产品信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductInfoRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "19133")
    private Long id;

    @Schema(description = "图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    private String pictureUrl;

    @Schema(description = "skuId", requiredMode = Schema.RequiredMode.REQUIRED, example = "1558")
    @ExcelProperty("skuId")
    private String skuId;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("产品名称")
    private String skuName;

    @Schema(description = "规格说明")
    private String specification;

    @Schema(description = "Spu", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Spu")
    private String modelNumber;

    @Schema(description = "单位")
    @ExcelProperty(value = "单位", converter = DictConvert.class)
    @DictFormat("dm_unit_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private String unit;

    @Schema(description = "售卖状态", example = "2")
    @ExcelProperty(value = "售卖状态", converter = DictConvert.class)
    @DictFormat("dm_sale_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer saleStatus;

    @Schema(description = "类目ID", example = "18649")
    private Long categoryId;

    @Schema(description = "品牌ID", example = "9982")
    @ExcelProperty("品牌ID")
    private Long brandId;

    @Schema(description = "标签", example = "4299")
    @ExcelProperty(value = "标签", converter = DictConvert.class)
    @DictFormat("dm_product_flag") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer flagId;

    @Schema(description = "预估成本价", example = "11247")
    @ExcelProperty("采购价")
    private BigDecimal costPrice;

    @Schema(description = "产品描述", example = "你猜")
    private String description;

    @Schema(description = "类目佣金")
    private BigDecimal categoryCommission;

    @Schema(description = "类目佣金Id", example = "28811")
    private Long categoryCommissionId;

    @Schema(description = "目标平台")
    @ExcelProperty(value = "目标平台", converter = DictConvert.class)
    @DictFormat("dm_platform") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer platform;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "产品规格信息")
    @ExcelProperty("产品规格")
    private String purchaseInfo;

    @Schema(description = "体积")
    @ExcelProperty("体积")
    private BigDecimal volume;

    @Schema(description = "毛重")
    @ExcelProperty("毛重")
    private BigDecimal grossWeight;

    @Schema(description = "箱规")
    @ExcelProperty("箱规")
    private String boxInfo;

    @Schema(description = "装箱率")
    @ExcelProperty("装箱率")
    private Integer pcs;

}