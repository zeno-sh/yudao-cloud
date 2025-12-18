package cn.iocoder.yudao.module.report.convert.jimu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 头程计划 Response VO")
@Data
public class TransportPlanItemRespVO {

    private Integer id;
    /**
     * 头程计划ID
     */
    private Long planId;
    /**
     * 采购计划单号
     */
    private String planNumber;
    /**
     * 采购单号
     */
    private String poNumber;
    /**
     * 导出使用
     */
    private String productImage;
    /**
     * 导出使用
     */
    private String productSkuId;
    /**
     * 导出使用
     */
    private String productSkuName;
    /**
     * 产品Id
     */
    private Long productId;
    /**
     * 产品信息
     */
    private ProductSimpleInfoVO productSimpleInfo;
    /**
     * 采购单详情ID
     */
    private Long purchaseOrderItemId;
    /**
     * 发运数量
     */
    private Integer quantity;
    /**
     * pcs
     */
    private Integer pcs;
    /**
     * 箱数
     */
    private Integer numberOfBox;
    /**
     * 体积
     */
    private BigDecimal volume;
    /**
     * 重量
     */
    private BigDecimal weight;
    /**
     * 运营
     */
    private String operatorName;
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    /**
     * 含税总金额
     */
    private BigDecimal totalTaxAmount;
    /**
     * 海外仓入库单号
     */
    private String overseaLocationCheckinId;
    /**
     * 上架状态
     *
     * 枚举 {@link TODO dm_first_choice 对应的类}
     */
    private String shelfStatus;
}