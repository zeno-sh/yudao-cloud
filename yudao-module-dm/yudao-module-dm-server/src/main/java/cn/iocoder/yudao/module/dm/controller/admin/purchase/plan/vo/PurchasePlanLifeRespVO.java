package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: Zeno
 * @createTime: 2024/06/13 10:34
 */
@Schema(description = "管理后台 - 采购计划进度")
@Data
@ExcelIgnoreUnannotated
public class PurchasePlanLifeRespVO {

    /**
     * 采购计划ID
     */
    private Long planId;
    /**
     * 计划批次编号
     */
    private String batchNumber;
    /**
     * 计划编号
     */
    private String planNumber;
    /**
     * 采购单明细ID
     */
    private Long orderItemId;
    /**
     * PO 采购单
     */
    private String purchaseOrderNumber;
    /**
     * 产品ID
     */
    private Long productId;
    /**
     * 头程计划编号
     */
    private String transportPlanNumber;
    /**
     * 采购数量
     */
    private Integer purchaseQuantity;
    /**
     * 发货数量
     */
    private Integer transportQuantity;
    /**
     * 预计抵达时间
     */
    private LocalDateTime arrivalDate;
    /**
     * 实际抵达时间
     */
    private LocalDateTime finishedDate;
    /**
     * 运营人员
     */
    private String operator;
    /**
     * 进度
     */
    private PurchasePlanItemStepsVO steps;
}
