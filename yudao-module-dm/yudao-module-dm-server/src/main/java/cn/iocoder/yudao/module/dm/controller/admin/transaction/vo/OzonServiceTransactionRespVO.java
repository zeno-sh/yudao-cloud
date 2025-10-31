package cn.iocoder.yudao.module.dm.controller.admin.transaction.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 交易记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OzonServiceTransactionRespVO {

    @Schema(description = "账单日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("账单日期")
    private String operationDate;

    @Schema(description = "门店id", example = "24908")
    @ExcelProperty("门店id")
    private String clientId;

    @Schema(description = "操作类型", example = "1")
    @ExcelProperty("操作类型")
    private String operationType;

    @Schema(description = "操作类型名称", example = "王五")
    @ExcelProperty("操作类型名称")
    private String operationTypeName;

    @Schema(description = "交易总金额")
    @ExcelProperty("交易总金额")
    private BigDecimal amount;

    @Schema(description = "收费类型", example = "1")
    @ExcelProperty("收费类型")
    private String type;


}