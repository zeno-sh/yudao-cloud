package cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 海外仓产品库存分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FbsProductStockPageReqVO extends PageParam {

    @Schema(description = "仓库ID", example = "29752")
    private Long warehouseId;

    @Schema(description = "仓库Sku")
    private String productSku;

    @Schema(description = "本地产品ID", example = "11463")
    private Long productId;

}