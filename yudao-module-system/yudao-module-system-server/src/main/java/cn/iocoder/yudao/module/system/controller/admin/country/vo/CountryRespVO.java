package cn.iocoder.yudao.module.system.controller.admin.country.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 国家信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CountryRespVO {

    @Schema(description = "国家", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("国家")
    private String country;

    @Schema(description = "地区", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("地区")
    private String region;

    @Schema(description = "地区名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("地区名称")
    private String regionName;

    @Schema(description = "币种", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("币种")
    private String currencyCode;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}