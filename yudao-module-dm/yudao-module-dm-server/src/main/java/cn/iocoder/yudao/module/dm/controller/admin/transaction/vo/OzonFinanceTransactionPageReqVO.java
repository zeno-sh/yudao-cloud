package cn.iocoder.yudao.module.dm.controller.admin.transaction.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 交易记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OzonFinanceTransactionPageReqVO extends PageParam {

    @Schema(description = "门店id", example = "24908")
    private String[] clientIds;

    @Schema(description = "操作ID", example = "21870")
    private Long operationId;

    @Schema(description = "发货单号，orders和returns时有值")
    private String postingNumber;

    @Schema(description = "操作类型", example = "1")
    private String operationType;

    @Schema(description = "操作日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] operationDate;

    @Schema(description = "收费类型", example = "1")
    private String type;

    @Schema(description = "服务信息（JSON格式）")
    private String services;

    @Schema(description = "分组类型", example = "day、month、week")
    private String groupType;
}