package cn.iocoder.yudao.module.report.convert.jimu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 采购单 Response VO")
@Data
public class PurchaseOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25776")
    private Long id;

    @Schema(description = "采购单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @Schema(description = "预付比例")
    private Integer prepmentRatio;

    @Schema(description = "供应商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26720")
    private Long supplierId;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxxx公司")
    private String supplierName;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "附件url")
    private String attachments;

    @Schema(description = "结算账期")
    private LocalDateTime settleDate;

    @Schema(description = "单据负责人")
    private Integer owner;

    @Schema(description = "单据负责人")
    private String ownerName;

    @Schema(description = "单据创建人")
    private String creator;

    @Schema(description = "单据创建人")
    private String creatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "结算方式 10=现结 20=月结", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer settleType;

    @Schema(description = "是否含税 0=不含 1=含", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean tax;

    @Schema(description = "运费", example = "32533")
    private BigDecimal transportationPrice;

    @Schema(description = "其他费用", example = "21650")
    private BigDecimal otherPrice;

    @Schema(description = "合计金额", example = "26081")
    private BigDecimal totalPrice;

    @Schema(description = "合计货值", example = "16913")
    private BigDecimal totalProductPrice;

    @Schema(description = "合计数量", example = "31238")
    private Integer totalCount;

    @Schema(description = "已付款金额", example = "13784")
    private BigDecimal paymentPrice;

    @Schema(description = "合计税额", example = "2642")
    private Integer totalTaxPrice;

    @Schema(description = "采购状态", example = "2")
    private Integer status;

    @Schema(description = "已到货数量", example = "2")
    private Integer arriveQuantity;

    @Schema(description = "关联的采购计划批次号", example = "2")
    private String batchNumber;

    @Schema(description = "预计到货时间")
    private LocalDateTime arrivalDate;
}