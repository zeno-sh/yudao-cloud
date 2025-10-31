package cn.iocoder.yudao.module.dm.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;

@Schema(description = "管理后台 - Ozon订单新增/修改 Request VO")
@Data
public class OzonOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15047")
    private Long id;

    @Schema(description = "平台门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "22208")
    @NotEmpty(message = "平台门店id不能为空")
    private String clientId;

    @Schema(description = "租户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22208")
    private Long tenantId;

    @Schema(description = "平台订单id", requiredMode = Schema.RequiredMode.REQUIRED, example = "287")
    @NotEmpty(message = "平台订单id不能为空")
    private String orderId;

    @Schema(description = "发货编号")
    private String postingNumber;

    @Schema(description = "父发货编号")
    private String parentPostingNumber;

    @Schema(description = "订单编号")
    private String orderNumber;

    @Schema(description = "订单状态", example = "1")
    private String status;

    @Schema(description = "接单时间")
    private LocalDateTime inProcessAt;

    @Schema(description = "发运时间")
    private LocalDateTime shipmentDate;

    @Schema(description = "交货时间")
    private LocalDateTime deliveringDate;

    @Schema(description = "订单销售金额")
    private BigDecimal accrualsForSale;

    @Schema(description = "取消原因")
    private String cancellation;

    @Schema(description = "商品快照")
    private String products;

    @Schema(description = "仓库信息")
    private String deliveryMethod;

    @Schema(description = "是否FBS")
    private String fbs;

    @Schema(description = "订单子状态", example = "2")
    private String substatus;

    @Schema(description = "发货条码")
    private String barcode;

    @Schema(description = "订单类型")
    private Integer orderType;

    @Schema(description = "订单类型")
    private List<Long> fbsWarehouseIds;

    @Schema(description = "订单商品详情列表")
    private List<OzonOrderItemDO> ozonOrderItems;

}