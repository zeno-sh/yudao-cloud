package cn.iocoder.yudao.module.dm.controller.admin.supplier.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 供应商信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductSupplierRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5693")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("供应商名称")
    private String supplierName;

    @Schema(description = "供应商代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("供应商代码")
    private String supplierCode;

    @Schema(description = "网址")
    @ExcelProperty("网址")
    private String website;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "源头工厂")
    @ExcelProperty(value = "源头工厂", converter = DictConvert.class)
    @DictFormat("dm_first_choice") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private String sourceFactory;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}