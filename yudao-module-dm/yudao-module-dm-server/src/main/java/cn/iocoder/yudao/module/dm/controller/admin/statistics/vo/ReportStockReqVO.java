package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author: Zeno
 * @createTime: 2024/12/25 20:34
 */
@Data
public class ReportStockReqVO extends ReportBaseRequest {


    private Long productId;

    @Schema(description = "月份，格式为yyyy-MM", required = true, example = "2024-01")
    @NotEmpty(message = "月份不能为空")
    @Pattern(regexp = "^\\d{4}-(?:0[1-9]|1[0-2])$", message = "月份格式不正确")
    private String month;
}
