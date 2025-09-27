package cn.iocoder.yudao.module.dm.controller.admin.transaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 交易记录新增/修改 Request VO")
@Data
public class OzonFinanceTransactionSaveReqVO {

    @Schema(description = "自动递增主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "21669")
    private Long id;

    private Long tenantId;

    @Schema(description = "门店id", example = "24908")
    private String clientId;

    @Schema(description = "操作ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21870")
    @NotNull(message = "操作ID不能为空")
    private Long operationId;

    @Schema(description = "发货单号，orders和returns时有值")
    private String postingNumber;

    @Schema(description = "操作类型", example = "1")
    private String operationType;

    @Schema(description = "操作日期")
    private LocalDate operationDate;

    @Schema(description = "操作类型名称", example = "王五")
    private String operationTypeName;

    @Schema(description = "配送费用（超大件）")
    private BigDecimal deliveryCharge;

    @Schema(description = "退货配送费用（超大件）")
    private BigDecimal returnDeliveryCharge;

    @Schema(description = "销售应计金额")
    private BigDecimal accrualsForSale;

    @Schema(description = "销售佣金或返还")
    private BigDecimal saleCommission;

    @Schema(description = "交易总金额")
    private BigDecimal amount;

    @Schema(description = "收费类型", example = "1")
    private String type;

    @Schema(description = "发货信息（JSON格式）")
    private String posting;

    @Schema(description = "商品信息（JSON格式）")
    private String items;

    @Schema(description = "服务信息（JSON格式）")
    private String services;

}