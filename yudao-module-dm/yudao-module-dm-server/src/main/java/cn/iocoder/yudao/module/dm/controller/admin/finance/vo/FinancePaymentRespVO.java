package cn.iocoder.yudao.module.dm.controller.admin.finance.vo;

import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentItemDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 付款单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FinancePaymentRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17550")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "付款单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("付款单号")
    private String no;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态")
    private Integer auditStatus;

    @Schema(description = "合计金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1586")
    @ExcelProperty("合计金额")
    private BigDecimal totalPrice;

    @Schema(description = "实付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "997")
    @ExcelProperty("实付金额")
    private BigDecimal paymentPrice;

    @Schema(description = "优惠金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "997")
    @ExcelProperty("优惠金额")
    private BigDecimal discountPrice;

    @Schema(description = "财务人员", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("财务人员")
    private Long owner;

    @Schema(description = "财务人员", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("财务人员")
    private String ownerName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "BPM审批流程ID")
    @ExcelProperty("BPM审批流程ID")
    private String processInstanceId;

    private List<FinancePaymentItemRespVO> financePaymentItems;

    @Schema(description = "图片")
    private List<String> picUrls;
}