package cn.iocoder.yudao.module.dm.controller.admin.shop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ozon店铺新增/修改 Request VO")
@Data
public class OzonShopMappingSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4508")
    private Integer id;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "平台不能为空")
    private Integer platform;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "门店名称不能为空")
    private String shopName;

    @Schema(description = "平台门店Id", requiredMode = Schema.RequiredMode.REQUIRED, example = "31283")
    @NotEmpty(message = "平台门店Id不能为空")
    private String clientId;

    @Schema(description = "密钥", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "密钥不能为空")
    private String apiKey;

    @Schema(description = "备用API密钥")
    private String apiKey2;

    @Schema(description = "广告key", example = "18890")
    private String adClientId;

    @Schema(description = "广告密钥")
    private String adClientSecret;

    @Schema(description = "API密钥过期时间")
    private LocalDateTime apiExpireTime;

    @Schema(description = "授权状态", example = "10")
    private Integer authStatus;

}