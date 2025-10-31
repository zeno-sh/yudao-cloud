package cn.iocoder.yudao.module.dm.controller.admin.transaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 交易记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OzonFinanceTransactionRespVO {

    @Schema(description = "自动递增主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21669")
    @ExcelProperty("自动递增主键")
    private Long id;

    @Schema(description = "门店id", example = "24908")
    @ExcelProperty("门店id")
    private String clientId;
    private String shopName;
    private Integer platform;

    @Schema(description = "操作ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21870")
    @ExcelProperty("操作ID")
    private Long operationId;

    @Schema(description = "发货单号，orders和returns时有值")
    @ExcelProperty("发货单号，orders和returns时有值")
    private String postingNumber;

    @Schema(description = "操作类型", example = "1")
    @ExcelProperty("操作类型")
    private String operationType;

    @Schema(description = "操作日期")
    @ExcelProperty("操作日期")
    private LocalDateTime operationDate;

    @Schema(description = "操作类型名称", example = "王五")
    @ExcelProperty("操作类型名称")
    private String operationTypeName;

    @Schema(description = "配送费用（超大件）")
    @ExcelProperty("配送费用（超大件）")
    private BigDecimal deliveryCharge;

    @Schema(description = "退货配送费用（超大件）")
    @ExcelProperty("退货配送费用（超大件）")
    private BigDecimal returnDeliveryCharge;

    @Schema(description = "销售应计金额")
    @ExcelProperty("销售应计金额")
    private BigDecimal accrualsForSale;

    @Schema(description = "销售佣金或返还")
    @ExcelProperty("销售佣金或返还")
    private BigDecimal saleCommission;

    @Schema(description = "交易总金额")
    @ExcelProperty("交易总金额")
    private BigDecimal amount;

    @Schema(description = "收费类型", example = "1")
    @ExcelProperty("收费类型")
    private String type;

    @Schema(description = "发货信息（JSON格式）")
    @ExcelProperty("发货信息（JSON格式）")
    private String posting;

    @Schema(description = "商品信息（JSON格式）")
    @ExcelProperty("商品信息（JSON格式）")
    private String items;

    @Schema(description = "服务信息（JSON格式）")
    @ExcelProperty("服务信息（JSON格式）")
    private String services;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "送货费用")
    @ExcelProperty("送货费用")
    private BigDecimal deliverAmount;
}