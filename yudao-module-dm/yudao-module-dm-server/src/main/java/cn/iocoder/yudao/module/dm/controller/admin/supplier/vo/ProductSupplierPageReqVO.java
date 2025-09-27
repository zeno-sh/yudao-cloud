package cn.iocoder.yudao.module.dm.controller.admin.supplier.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 供应商信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSupplierPageReqVO extends PageParam {

    @Schema(description = "供应商名称", example = "张三")
    private String supplierName;

    @Schema(description = "供应商代码")
    private String supplierCode;

    @Schema(description = "源头工厂")
    private String sourceFactory;

}