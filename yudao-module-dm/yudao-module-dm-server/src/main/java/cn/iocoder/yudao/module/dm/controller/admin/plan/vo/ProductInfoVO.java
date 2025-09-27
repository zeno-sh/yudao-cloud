package cn.iocoder.yudao.module.dm.controller.admin.plan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/04/19 17:25
 */
@Schema(description = "管理后台 - 产品信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductInfoVO {

    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("产品名称")
    private String skuName;

    @Schema(description = "图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    @ExcelProperty("图片")
    private String pictureUrl;

    private String skuId;

    /**
     * 体积
     */
    private String volume;
    /**
     * 体积重 长宽高/6000
     */
    private String volumeWeight;
    /**
     * 体积升 长宽高mm/1000000
     */
    private String volumeRise;
    /**
     * 密度 重量kg/体积m3
     */
    private String density;

    private ProductPurchaseVO productPurchase;

}
