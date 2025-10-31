package cn.iocoder.yudao.module.dm.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - Ozon订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OzonOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15047")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "平台门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "22208")
    @ExcelProperty("平台门店id")
    private String clientId;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "22208")
    @ExcelProperty("门店名称")
    private String shopName;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "22208")
    @ExcelProperty("平台")
    private Integer platform;

    @Schema(description = "平台订单id", requiredMode = Schema.RequiredMode.REQUIRED, example = "287")
    @ExcelProperty("平台订单id")
    private String orderId;

    @Schema(description = "发货编号")
    @ExcelProperty("发货编号")
    private String postingNumber;

    @Schema(description = "父发货编号")
    @ExcelProperty("父发货编号")
    private String parentPostingNumber;

    @Schema(description = "订单编号")
    @ExcelProperty("订单编号")
    private String orderNumber;

    @Schema(description = "订单状态", example = "1")
    @ExcelProperty("订单状态")
    private String status;

    @Schema(description = "接单时间")
    @ExcelProperty("接单时间")
    private LocalDateTime inProcessAt;

    @Schema(description = "发运时间")
    @ExcelProperty("发运时间")
    private LocalDateTime shipmentDate;

    @Schema(description = "交货时间")
    @ExcelProperty("交货时间")
    private LocalDateTime deliveringDate;

    @Schema(description = "订单销售金额")
    @ExcelProperty("订单销售金额")
    private BigDecimal accrualsForSale;

    @Schema(description = "取消原因")
    @ExcelProperty("取消原因")
    private String cancellation;

    @Schema(description = "商品快照")
    @ExcelProperty("商品快照")
    private String products;

    @Schema(description = "仓库信息")
    @ExcelProperty("仓库信息")
    private String deliveryMethod;

    @Schema(description = "是否FBS")
    @ExcelProperty("是否FBS")
    private String fbs;

    @Schema(description = "订单子状态", example = "2")
    @ExcelProperty("订单子状态")
    private String substatus;

    @Schema(description = "订单类型", example = "2")
    @ExcelProperty("订单类型")
    private Integer orderType;

    @Schema(description = "发货条码")
    @ExcelProperty("发货条码")
    private String barcode;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "产品信息")
    private List<OzonOrderItemRespVO> items;

    @Schema(description = "是否可推送")
    private Integer pushStatus;

    @Schema(description = "推送记录")
    private List<FbsPushInfoVO> pushInfo;
}