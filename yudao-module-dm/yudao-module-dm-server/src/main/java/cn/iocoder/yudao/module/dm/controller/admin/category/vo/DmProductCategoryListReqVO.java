package cn.iocoder.yudao.module.dm.controller.admin.category.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 产品分类列表 Request VO")
@Data
public class DmProductCategoryListReqVO {

    @Schema(description = "分类名称", example = "李四")
    private String name;

    @Schema(description = "分类编码")
    private String code;

}