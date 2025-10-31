package cn.iocoder.yudao.module.dm.controller.admin.commission.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 类目佣金列表 Request VO")
@Data
public class CategoryCommissionListReqVO {

    @Schema(description = "平台", example = "17164")
    private Integer platformId;

    @Schema(description = "类目名称", example = "芋艿")
    private String categoryName;

    @Schema(description = "父类目ID", example = "123")
    private Long parentId;

}