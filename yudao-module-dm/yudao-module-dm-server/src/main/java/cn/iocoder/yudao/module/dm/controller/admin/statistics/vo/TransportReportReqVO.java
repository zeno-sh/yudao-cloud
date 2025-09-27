package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 发货计划报表 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TransportReportReqVO extends PageParam {

    @Schema(description = "月份，格式为：yyyy-MM", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-12")
    @NotEmpty(message = "月份不能为空")
    private String month;

    @Schema(description = "产品编号", example = "1024")
    private Long productId;
} 