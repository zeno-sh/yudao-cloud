package cn.iocoder.yudao.module.dm.controller.admin.transport.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.dm.enums.DictTypeConstant;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 头程计划明细查询 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TransportPlanDetailRespVO extends TransportPlanItemRespVO {

    @Schema(description = "发运时间")
    @ExcelProperty(value = "发运时间")
    private LocalDateTime despatchDate;

    @Schema(description = "预计抵达时间")
    @ExcelProperty(value = "预计抵达时间")
    private LocalDateTime arrivalDate;

    @Schema(description = "实际到达时间")
    @ExcelProperty(value = "实际到达时间")
    private LocalDateTime finishedDate;

    @Schema(description = "货代公司")
    @ExcelProperty(value = "货代公司")
    private String forwarder;

    @Schema(description = "发货计划编号")
    @ExcelProperty(value = "发货计划编号")
    private String code;

    @Schema(description = "运输状态", example = "2")
    @ExcelProperty(value = "运输状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstant.DM_TRANSPORT_STATUS)
    private Integer transportStatus;
} 