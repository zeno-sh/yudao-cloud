package cn.iocoder.yudao.module.dm.controller.admin.product.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 产品信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductInfoPageReqVO extends PageParam {

    @Schema(description = "skuId", example = "1558")
    private String skuId;

    @Schema(description = "产品名称", example = "芋艿")
    private String skuName;

    @Schema(description = "售卖状态", example = "2")
    private Integer saleStatus;

    @Schema(description = "类目ID", example = "18649")
    private Long categoryId;

    @Schema(description = "品牌ID", example = "9982")
    private Long brandId;

    @Schema(description = "标签", example = "4299")
    private Integer flagId;

    @Schema(description = "目标平台")
    private Integer platform;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "供应商代码", example = "SUP001")
    private String supplierCode;

}