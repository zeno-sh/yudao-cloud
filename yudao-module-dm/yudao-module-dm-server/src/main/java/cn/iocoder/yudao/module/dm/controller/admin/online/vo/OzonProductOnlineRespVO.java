package cn.iocoder.yudao.module.dm.controller.admin.online.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 在线商品 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OzonProductOnlineRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "27790")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "门店", requiredMode = Schema.RequiredMode.REQUIRED, example = "10269")
    @ExcelProperty("门店")
    private String clientId;

    private Integer platform;

    private String shopName;

    @Schema(description = "本地Sku", example = "24537")
    @ExcelProperty("本地Sku")
    private String skuId;

    @Schema(description = "平台货号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22077")
    @ExcelProperty("平台货号")
    private String offerId;

    @Schema(description = "平台Sku", example = "24438")
    @ExcelProperty("平台Sku")
    private String platformSkuId;

    @Schema(description = "平台商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22647")
    @ExcelProperty("平台商品ID")
    private String productId;

    @Schema(description = "主图")
    @ExcelProperty("主图")
    private String image;

    @Schema(description = "是否大宗商品")
    @ExcelProperty("是否大宗商品")
    private Boolean isKgt;

    @Schema(description = "补贴价", example = "1158")
    @ExcelProperty("补贴价")
    private BigDecimal marketingPrice;

    @Schema(description = "价格", example = "30211")
    @ExcelProperty("价格")
    private BigDecimal price;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createAt;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "本地产品ID", example = "17933")
    @ExcelProperty("本地产品ID")
    private Long dmProductId;

    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("dm_ozon_product_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

}