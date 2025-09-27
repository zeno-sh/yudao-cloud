package cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Schema(description = "管理后台 - 采购单到货日志新增/修改 Request VO")
@Data
public class PurchaseOrderArrivedLogListSaveReqVO {

    @Schema(description = "到货记录", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(message = "到货记录不能为空", min = 1)
    private List<PurchaseOrderArrivedLogSaveReqVO> arrivedLogList;

}