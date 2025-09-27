package cn.iocoder.yudao.module.dm.controller.admin.purchase.order.vo;

import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author: Zeno
 * @createTime: 2024/05/14 11:27
 */
@Data
@ExcelIgnoreUnannotated
public class PurchaseOrderItemVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "采购单ID")
    private Long orderId;

    @Schema(description = "采购单编号")
    private String orderNo;

    @Schema(description = "采购计划编号")
    @ExcelProperty("采购计划编号")
    private String planNumber;

    @Schema(description = "Sku")
    @ExcelProperty("Sku")
    private String sku;

    @Schema(description = "产品ID")
    private Long productId;

    @Schema(description = "单箱数量")
    private Integer pcs;

    @Schema(description = "单价")
    @ExcelProperty("单价")
    private BigDecimal price;

    @Schema(description = "含税单价")
    @ExcelProperty("含税单价")
    private BigDecimal taxPrice;

    @Schema(description = "税率")
    @ExcelProperty("税率")
    private BigDecimal taxRate;

    @Schema(description = "含税")
    @ExcelProperty("含税")
    private Boolean tax;

    @Schema(description = "采购数量")
    @ExcelProperty("采购数量")
    private Integer purchaseQuantity;

    @Schema(description = "预计到货时间")
    private LocalDateTime arrivalDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "税额（单价*税率*数量）")
    @ExcelProperty("税额（单价*税率*数量）")
    private BigDecimal taxAmount;

    @Schema(description = "总金额（单价*采购数量）")
    @ExcelProperty("总金额（单价*采购数量）")
    private BigDecimal totalAmount;

    @Schema(description = "含税总金额")
    @ExcelProperty("含税总金额")
    private BigDecimal totalTaxAmount;
    /**
     * @see cn.iocoder.yudao.module.dm.enums.PurchaseOrderStatusEnum
     */
    @ExcelProperty("采购状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建人名称")
    private String creatorName;

    @Schema(description = "已到货数量")
    private Integer totalArrivedQuantity;

    @Schema(description = "已发运数量")
    private Integer totalShippedQuantity;

    @Schema(description = "可发运数量")
    private Integer remainingQuantity;

    @Schema(description = "运营人员")
    private String operateName;

    @Schema(description = "期望到货时间")
    private LocalDateTime expectedArrivalDate;

    @Schema(description = "产品信息")
    private ProductSimpleInfoVO productSimpleInfo;

    @Schema(description = "体积")
    private BigDecimal volume;

    @Schema(description = "重量")
    private BigDecimal weight;

    @Schema(description = "箱数")
    private Integer numberOfBox;

    @Schema(description = "图片")
    private String image;

    @Schema(description = "产品名称")
    private String skuName;
}
