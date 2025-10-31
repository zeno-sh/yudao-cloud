package cn.iocoder.yudao.module.dm.controller.admin.shop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - ozon店铺 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OzonShopMappingRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4508")
    @ExcelProperty("主键")
    private Integer id;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "平台", converter = DictConvert.class)
    @DictFormat("dm_platform") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer platform;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("门店名称")
    private String shopName;

    @Schema(description = "平台门店Id", requiredMode = Schema.RequiredMode.REQUIRED, example = "31283")
    @ExcelProperty("平台门店Id")
    private String clientId;

    @Schema(description = "密钥", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("密钥")
    private String apiKey;

    @Schema(description = "备用API密钥")
    @ExcelProperty("备用API密钥")
    private String apiKey2;

    @Schema(description = "广告key", example = "18890")
    @ExcelProperty("广告key")
    private String adClientId;

    @Schema(description = "广告密钥")
    @ExcelProperty("广告密钥")
    private String adClientSecret;

    @Schema(description = "API密钥过期时间")
    @ExcelProperty("API密钥过期时间")
    private LocalDateTime apiExpireTime;

    @Schema(description = "授权状态：10-正常 20-已过期 30-已禁用 40-待审核", example = "10")
    @ExcelProperty(value = "授权状态", converter = DictConvert.class)
    @DictFormat("dm_auth_status")
    private Integer authStatus;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}