package cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 采购单到货日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PurchaseOrderArrivedLogPageReqVO extends PageParam {

    @Schema(description = "采购单ID", example = "3160")
    private Long purchaseOrderId;

    @Schema(description = "采购单item ID", example = "10027")
    private Long purchaseOrderItemId;

    @Schema(description = "到货数量")
    private Integer arrivedQuantity;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}