package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 采购计划 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PurchasePlanRespVO {

    @Schema(description = "批次编号")
    @ExcelProperty("批次编号")
    private String batchNumber;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4271")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "总采购量")
    @ExcelProperty("总采购量")
    private Integer totalQuantity;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    private String creator;

    @Schema(description = "创建人")
    @ExcelProperty("创建人")
    private String creatorName;

    @Schema(description = "bpm审批流程Id", example = "562")
    @ExcelProperty("bpm审批流程Id")
    private String processInstanceId;

    @Schema(description = "审批状态", example = "2")
    @ExcelProperty(value = "审批状态", converter = DictConvert.class)
    @DictFormat("bpm_task_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer auditStatus;

    private List<PurchasePlanItemRespVO> purchasePlanItems;
}