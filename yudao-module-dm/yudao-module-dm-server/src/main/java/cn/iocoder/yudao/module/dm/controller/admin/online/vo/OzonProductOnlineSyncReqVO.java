package cn.iocoder.yudao.module.dm.controller.admin.online.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 在线商品新增/修改 Request VO")
@Data
public class OzonProductOnlineSyncReqVO {

    @Schema(description = "门店", requiredMode = Schema.RequiredMode.REQUIRED, example = "10269")
    private String[] clientIds;

    @Schema(description = "平台货号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22077")
    private String offerId;

    @Schema(description = "本地产品ID", example = "17933")
    private Long dmProductId;

}