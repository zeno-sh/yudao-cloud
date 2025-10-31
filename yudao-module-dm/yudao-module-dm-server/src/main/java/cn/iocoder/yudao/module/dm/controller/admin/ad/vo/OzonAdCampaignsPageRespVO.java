package cn.iocoder.yudao.module.dm.controller.admin.ad.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 广告活动 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OzonAdCampaignsPageRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "5183")
    @ExcelProperty("主键")
    private Integer id;

    @Schema(description = "门店ID", example = "8817")
    @ExcelProperty("门店ID")
    private String clientId;

    @Schema(description = "展示量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("展示量")
    private Integer views;

    @Schema(description = "点击数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("点击数")
    private Integer clicks;

    @Schema(description = "广告花费", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("广告花费")
    private BigDecimal moneySpent;

    @Schema(description = "平均报价", requiredMode = Schema.RequiredMode.REQUIRED, example = "29986")
    @ExcelProperty("平均报价")
    private BigDecimal avgBid;

    @Schema(description = "订单数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("订单数量")
    private Integer orders;

    @Schema(description = "订单金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("订单金额")
    private BigDecimal ordersMoney;

    @Schema(description = "日期")
    @ExcelProperty("日期")
    private String date;


}