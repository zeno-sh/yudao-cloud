package cn.iocoder.yudao.module.dm.controller.admin.supplier.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 供应商信息新增/修改 Request VO")
@Data
public class ProductSupplierSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5693")
    private Long id;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "供应商名称不能为空")
    private String supplierName;

    @Schema(description = "供应商代码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String supplierCode;

    @Schema(description = "网址")
    private String website;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "源头工厂")
    private String sourceFactory;

}