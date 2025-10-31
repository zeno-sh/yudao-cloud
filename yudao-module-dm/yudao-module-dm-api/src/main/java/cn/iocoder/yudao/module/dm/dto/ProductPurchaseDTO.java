package cn.iocoder.yudao.module.dm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 产品采购信息 DTO
 *
 * @author Jax
 */
@Data
public class ProductPurchaseDTO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * SKU
     */
    private String skuId;

    /**
     * 供应商代码
     */
    private String supplierCode;

    /**
     * 箱规名称
     */
    private String cartonSizeName;

    /**
     * 单品长（cm）
     */
    private BigDecimal length;

    /**
     * 单品宽（cm）
     */
    private BigDecimal width;

    /**
     * 单品高（cm）
     */
    private BigDecimal height;

    /**
     * 单品净重量（g）
     */
    private BigDecimal netWeight;

    /**
     * 单品毛重量（g）
     */
    private BigDecimal grossWeight;

    /**
     * 产品材质
     */
    private String material;

    /**
     * 箱规长（cm）
     */
    private BigDecimal boxLength;

    /**
     * 箱规宽（cm）
     */
    private BigDecimal boxWidth;

    /**
     * 箱规高（cm）
     */
    private BigDecimal boxHeight;

    /**
     * 每箱子的产品数（pcs）
     */
    private Integer quantityPerBox;

    /**
     * 箱重（g）
     */
    private BigDecimal boxWeight;

    /**
     * 首选
     */
    private String firstChoice;

    /**
     * 计算单品体积（立方米）
     * 
     * @return 体积（立方米）
     */
    public BigDecimal calculateVolume() {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        // 长宽高单位是cm，转换为立方米：cm³ / 1,000,000 = m³
        return length.multiply(width).multiply(height)
                .divide(new BigDecimal("1000000"), 6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算单品重量（kg）
     * 
     * @return 重量（kg）
     */
    public BigDecimal calculateWeight() {
        if (grossWeight == null) {
            return BigDecimal.ZERO;
        }
        // 毛重单位是g，转换为kg：g / 1000 = kg
        return grossWeight.divide(new BigDecimal("1000"), 3, BigDecimal.ROUND_HALF_UP);
    }
} 