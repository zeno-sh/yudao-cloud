package cn.iocoder.yudao.module.dm.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentItemDO;

@Schema(description = "管理后台 - 付款单新增/修改 Request VO")
@Data
public class FinancePaymentSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17550")
    private Long id;

    @Schema(description = "付款单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String no;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer audiStatus;

    @Schema(description = "合计金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1586")
    @NotNull(message = "合计金额不能为空")
    private BigDecimal totalPrice;

    @Schema(description = "实付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "997")
    @NotNull(message = "实付金额不能为空")
    private BigDecimal paymentPrice;

    @Schema(description = "优惠金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "997")
    private BigDecimal discountPrice;

    @Schema(description = "财务人员")
    private Long owner;

    @Schema(description = "付款项列表")
    private List<FinancePaymentItemDO> financePaymentItems;

    @Schema(description = "图片")
    private List<String> picUrls;
}