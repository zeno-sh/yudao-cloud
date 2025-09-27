package cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo;

import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema(description = "管理后台 - Ozon FBO进仓报表 Response VO")
@Data
@Accessors(chain = true)
public class OzonFboInboundReportRespVO {

    @Schema(description = "月份，格式为yyyy-MM")
    private String month;

    @Schema(description = "商品信息")
    private ProductSimpleInfoVO productSimpleInfo;

    @Schema(description = "期初结余")
    private Integer initialBalance;

    @Schema(description = "本期进仓数量")
    private Integer inboundQuantity;

    @Schema(description = "本期销售数量")
    private Integer salesQuantity;

    @Schema(description = "期末结余")
    private Integer finalBalance;

    @Schema(description = "供应商报价")
    private BigDecimal supplierPrice;

    @Schema(description = "含税金额")
    private BigDecimal taxIncludedAmount;

    @Schema(description = "不含税金额")
    private BigDecimal taxExcludedAmount;

} 