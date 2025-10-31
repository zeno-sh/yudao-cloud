package cn.iocoder.yudao.module.dm.controller.admin.finance.vo;

import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentItemDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 付款单新增/修改 Request VO")
@Data
public class FinancePaymentSaveFileReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17550")
    private Long id;

    @Schema(description = "图片")
    @Size(min = 1, message = "付款凭证不能为空")
    private List<String> picUrls;

}