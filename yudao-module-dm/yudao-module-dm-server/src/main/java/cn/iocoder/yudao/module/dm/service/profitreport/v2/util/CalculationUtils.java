package cn.iocoder.yudao.module.dm.service.profitreport.v2.util;

import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.CalculationConfig;

import java.math.BigDecimal;

/**
 * 计算工具类
 * 统一处理计算配置，避免硬编码
 *
 * @author Jax
 */
public class CalculationUtils {
    
    /**
     * 使用配置的精度和舍入模式进行乘法运算
     *
     * @param multiplicand 被乘数
     * @param multiplier 乘数
     * @param config 计算配置
     * @return 计算结果
     */
    public static BigDecimal multiply(BigDecimal multiplicand, BigDecimal multiplier, CalculationConfig config) {
        BigDecimal result = multiplicand.multiply(multiplier);
        if (config == null) {
            // 兜底逻辑：使用默认配置
            return result.setScale(4, java.math.RoundingMode.HALF_UP);
        }
        return result.setScale(config.getPrecision(), config.getRoundingMode());
    }
    
    /**
     * 使用配置的精度和舍入模式进行除法运算
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @param config 计算配置
     * @return 计算结果
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, CalculationConfig config) {
        if (config == null) {
            // 兜底逻辑：使用默认配置
            return dividend.divide(divisor, 4, java.math.RoundingMode.HALF_UP);
        }
        return dividend.divide(divisor, config.getPrecision(), config.getRoundingMode());
    }
    
    /**
     * 使用配置的精度和舍入模式进行减法运算
     *
     * @param minuend 被减数
     * @param subtrahend 减数
     * @param config 计算配置
     * @return 计算结果
     */
    public static BigDecimal subtract(BigDecimal minuend, BigDecimal subtrahend, CalculationConfig config) {
        BigDecimal result = minuend.subtract(subtrahend);
        if (config == null) {
            // 兜底逻辑：使用默认配置
            return result.setScale(4, java.math.RoundingMode.HALF_UP);
        }
        return result.setScale(config.getPrecision(), config.getRoundingMode());
    }
    
    /**
     * 使用配置的精度和舍入模式进行加法运算
     *
     * @param augend 被加数
     * @param addend 加数
     * @param config 计算配置
     * @return 计算结果
     */
    public static BigDecimal add(BigDecimal augend, BigDecimal addend, CalculationConfig config) {
        BigDecimal result = augend.add(addend);
        if (config == null) {
            // 兜底逻辑：使用默认配置
            return result.setScale(4, java.math.RoundingMode.HALF_UP);
        }
        return result.setScale(config.getPrecision(), config.getRoundingMode());
    }
    
    /**
     * 使用配置的舍入模式设置精度
     *
     * @param value 原值
     * @param scale 精度
     * @param config 计算配置
     * @return 设置精度后的值
     */
    public static BigDecimal setScale(BigDecimal value, int scale, CalculationConfig config) {
        if (config == null) {
            return value.setScale(scale, java.math.RoundingMode.HALF_UP);
        }
        return value.setScale(scale, config.getRoundingMode());
    }
    
    /**
     * 百分比转换（除以100）
     *
     * @param percentage 百分比值
     * @param config 计算配置
     * @return 小数值
     */
    public static BigDecimal percentageToDecimal(BigDecimal percentage, CalculationConfig config) {
        return divide(percentage, new BigDecimal("100"), config);
    }
    
    /**
     * 重量转换（克转千克）
     *
     * @param grams 克数
     * @param config 计算配置
     * @return 千克数
     */
    public static BigDecimal gramsToKilograms(BigDecimal grams, CalculationConfig config) {
        return divide(grams, new BigDecimal("1000"), config);
    }
    
    /**
     * 体积转换（立方毫米转立方米）
     *
     * @param cubicMillimeters 立方毫米
     * @param config 计算配置
     * @return 立方米
     */
    public static BigDecimal cubicMillimetersToCubicMeters(BigDecimal cubicMillimeters, CalculationConfig config) {
        return divide(cubicMillimeters, new BigDecimal("1000000"), config);
    }
    
    /**
     * 计算密度（重量/体积）
     *
     * @param weight 重量
     * @param volume 体积
     * @param config 计算配置
     * @return 密度
     */
    public static BigDecimal calculateDensity(BigDecimal weight, BigDecimal volume, CalculationConfig config) {
        return divide(weight, volume, config);
    }
    
    /**
     * 单位成本计算
     *
     * @param totalCost 总成本
     * @param quantity 数量
     * @param config 计算配置
     * @return 单位成本
     */
    public static BigDecimal calculateUnitCost(BigDecimal totalCost, Integer quantity, CalculationConfig config) {
        return divide(totalCost, new BigDecimal(quantity), config);
    }
    
    /**
     * 利润率计算
     *
     * @param profit 利润
     * @param sales 销售额
     * @param config 计算配置
     * @return 利润率（保留2位小数）
     */
    public static BigDecimal calculateProfitRate(BigDecimal profit, BigDecimal sales, CalculationConfig config) {
        BigDecimal rate = divide(profit, sales, config);
        return setScale(rate, 2, config);
    }
} 