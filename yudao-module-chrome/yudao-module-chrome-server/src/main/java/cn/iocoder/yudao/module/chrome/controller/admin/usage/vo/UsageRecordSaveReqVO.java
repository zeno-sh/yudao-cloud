package cn.iocoder.yudao.module.chrome.controller.admin.usage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - Chrome使用记录新增/修改 Request VO")
@Data
public class UsageRecordSaveReqVO {

    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "20549")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25311")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "功能类型）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "功能类型不能为空")
    private Integer featureType;

    @Schema(description = "使用次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "9404")
    @NotNull(message = "使用次数不能为空")
    private Integer usageCount;

    @Schema(description = "商品ID", example = "1920")
    private String sellerProductId;
}