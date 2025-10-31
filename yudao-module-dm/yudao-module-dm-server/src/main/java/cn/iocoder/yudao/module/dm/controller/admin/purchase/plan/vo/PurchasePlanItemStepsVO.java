package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/06/04 14:20
 */
@Schema(description = "管理后台 - 采购计划进度")
@Data
@ExcelIgnoreUnannotated
public class PurchasePlanItemStepsVO {

    /**
     * 当前的步骤序号
     */
    private Integer currentStep;
    /**
     * 产品ID
     */
    private Long productId;
    /**
     * 计划编号
     */
    private String planNumber;
    /**
     * 进度详情
     */
    private List<PurchasePlanItemProcessVO> process;
}
