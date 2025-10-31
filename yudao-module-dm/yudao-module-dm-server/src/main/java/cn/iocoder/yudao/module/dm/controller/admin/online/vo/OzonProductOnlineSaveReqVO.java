package cn.iocoder.yudao.module.dm.controller.admin.online.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 在线商品新增/修改 Request VO")
@Data
public class OzonProductOnlineSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "27790")
    private Long id;

    private Long tenantId;

    @Schema(description = "门店", requiredMode = Schema.RequiredMode.REQUIRED, example = "10269")
    @NotEmpty(message = "门店不能为空")
    private String clientId;

    @Schema(description = "本地Sku", example = "24537")
    private String skuId;

    @Schema(description = "平台货号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22077")
    @NotEmpty(message = "平台货号不能为空")
    private String offerId;

    @Schema(description = "平台Sku", example = "24438")
    private String platformSkuId;

    @Schema(description = "平台商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22647")
    @NotEmpty(message = "平台商品ID不能为空")
    private String productId;

    @Schema(description = "主图")
    private String image;

    @Schema(description = "是否大宗商品")
    private Boolean isKgt;

    @Schema(description = "补贴价", example = "1158")
    private BigDecimal marketingPrice;

    @Schema(description = "价格", example = "30211")
    private BigDecimal price;

    @Schema(description = "创建时间")
    private LocalDateTime createAt;

    @Schema(description = "本地产品ID", example = "17933")
    private Long dmProductId;

    @Schema(description = "FBS海外仓", example = "17933")
    private List<Long> fbsWarehouseIds;

    @Schema(description = "归档")
    private Boolean isArchived;

    @Schema(description = "状态", example = "1")
    private Integer status;

}