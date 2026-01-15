package cn.iocoder.yudao.module.chrome.controller.admin.usage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - Chrome使用记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UsageRecordRespVO {

    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20549")
    @ExcelProperty("记录ID")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25311")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "功能类型（10商品采集 20排名采集 30评论采集 40销量采集 50趋势采集 60类目分析）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("功能类型（10商品采集 20排名采集 30评论采集 40销量采集 50趋势采集 60类目分析）")
    private Integer featureType;

    @Schema(description = "使用日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("使用日期")
    private LocalDate usageDate;

    @Schema(description = "使用次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "9404")
    @ExcelProperty("使用次数")
    private Integer usageCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "商品ID", example = "1")
    private String sellerProductId;
}