package cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author: Zeno
 * @createTime: 2024/05/20 16:39
 */
@Schema(description = "管理后台 - 采购单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PurchaseOrderItemPageReqVO extends PageParam {

    @Schema(description = "采购单ID")
    private Long orderId;

    @Schema(description = "产品ID")
    private Long productId;

    @Schema(description = "PO采购单编号")
    private String orderNo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "可发货")
    private Boolean shippedEnable;

    @Schema(description = "采购状态")
    private Integer status;
}
