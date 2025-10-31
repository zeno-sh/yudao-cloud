package cn.iocoder.yudao.module.dm.controller.admin.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

@Schema(description = "管理后台 - 广告活动同步 Request VO")
@Data
public class OzonAdCampaignsSyncReqVO {

    @Schema(description = "门店ID", example = "8817")
    private String[] clientIds;

    @Schema(description = "活动日期")
    @Size(min = 1, message = "活动日期不合法")
    private String[] date;

}