package cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo;

import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderItemDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 采购单新增/修改 Request VO")
@Data
public class PurchaseOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25776")
    private Long id;

    @Schema(description = "采购单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @Schema(description = "预付比例")
    private Integer prepmentRatio;

    @Schema(description = "供应商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26720")
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "附件url")
    private String attachments;

    @Schema(description = "结算账期")
    private LocalDateTime settleDate;

    @Schema(description = "单据负责人")
    private Integer owner;

    @Schema(description = "结算方式 10=现结 20=月结", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "结算方式不能为空")
    private Integer settleType;

    @Schema(description = "是否含税 0=不含 1=含", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否含税不能为空")
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

    @Schema(description = "采购单明细列表")
    @Size(min = 1, message = "采购单明细列表不能为空")
    private List<PurchaseOrderItemDO> purchaseOrderItems;

}