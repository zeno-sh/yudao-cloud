package cn.iocoder.yudao.module.dm.controller.admin.transport.vo;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.dm.enums.DictTypeConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.*;
import java.util.*;
import java.math.BigDecimal;


import java.time.LocalDateTime;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 头程计划 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TransportPlanRespVO {

    @Schema(description = "发货计划", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("发货计划")
    private String code;

    @Schema(description = "运输状态", example = "2")
    @ExcelProperty(value = "运输状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstant.DM_TRANSPORT_STATUS)
    private Integer transportStatus;

    @Schema(description = "海外仓入库单号", example = "7794")
    @ExcelProperty("海外仓入库单号")
    private String overseaLocationCheckinId;

    @Schema(description = "货代公司")
    @ExcelProperty("货代公司")
    private String forwarder;

    @Schema(description = "报价", example = "25376")
    @ExcelProperty("报价")
    private BigDecimal offerPrice;

    @Schema(description = "币种")
    @ExcelProperty(value = "币种", converter = DictConvert.class)
    @DictFormat(DictTypeConstant.DM_CURRENCY_CODE)
    private Integer currency;

    @Schema(description = "结算状态", example = "2")
    @ExcelProperty(value = "结算状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstant.DM_FIRST_CHOICE)
    private String settleStatus;

    @Schema(description = "账单金额", example = "8462")
    @ExcelProperty("账单金额")
    private BigDecimal billPrice;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "发运日期")
    @ExcelProperty("发运日期")
    private LocalDateTime despatchDate;

    @Schema(description = "预计抵达日期")
    @ExcelProperty("预计抵达日期")
    private LocalDateTime arrivalDate;

    @Schema(description = "实际抵达日期")
    @ExcelProperty("实际抵达日期")
    private LocalDateTime finishedDate;

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "28027")
    @ExcelIgnore
    private Long id;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    private String creatorName;

    @Schema(description = "创建人")
    @ExcelIgnore
    private String creator;

    @Schema(description = "文件")
    @ExcelIgnore
    private List<String> fileUrls;
}