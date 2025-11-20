package cn.iocoder.yudao.module.system.controller.admin.country.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 国家信息新增/修改 Request VO")
@Data
public class CountrySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15657")
    private Integer id;

    @Schema(description = "国家", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "国家不能为空")
    private String country;

    @Schema(description = "地区", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "地区不能为空")
    private String region;

    @Schema(description = "地区名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "地区名称不能为空")
    private String regionName;

    @Schema(description = "币种", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "币种不能为空")
    private String currencyCode;

}