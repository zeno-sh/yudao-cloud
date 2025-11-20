package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 利润预测分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProfitCalculationPageReqVO extends PageParam {

    @Schema(description = "本地产品ID", example = "31633")
    private Long productId;

    @Schema(description = "选品计划名称", example = "王五")
    private String planName;

    @Schema(description = "平台")
    private Integer platform;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "配置模板ID")
    private Long templateId;

    @Schema(description = "产品SKU")
    private String sku;

    @Schema(description = "售价", example = "25053")
    private BigDecimal price;

    @Schema(description = "币种")
    private Integer currency;

    @Schema(description = "汇率")
    private BigDecimal exchangeRate;

    @Schema(description = "采购价（CNY，不含税）")
    private BigDecimal purchaseCost;

    @Schema(description = "毛利率(%)范围 - 最小值")
    private BigDecimal grossMarginMin;

    @Schema(description = "毛利率(%)范围 - 最大值")
    private BigDecimal grossMarginMax;

    @Schema(description = "投资回报率(%)范围 - 最小值")
    private BigDecimal roiMin;

    @Schema(description = "投资回报率(%)范围 - 最大值")
    private BigDecimal roiMax;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}