package cn.iocoder.yudao.module.dm.dal.dataobject.template;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 利润计算配置模板 DO
 *
 * @author Zeno
 */
@TableName("dm_profit_calculation_template")
@KeySequence("dm_profit_calculation_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitCalculationTemplateDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 国家
     */
    private String country;
    /**
     * 平台
     */
    private Integer platform;
    /**
     * 体积系数（立方米转重量）
     */
    private BigDecimal volumeCoefficient;
    /**
     * 重量系数（重量转体积）
     */
    private BigDecimal weightCoefficient;
    /**
     * 国内运费单价（每立方米）
     */
    private BigDecimal domesticFreightUnit;
    /**
     * 货代费用单价（每立方米）
     */
    private BigDecimal freightForwarderUnit;
    /**
     * 关税率(%)
     */
    private BigDecimal tariffRate;
    /**
     * VAT税率(%)
     */
    private BigDecimal vatRate;
    /**
     * 申报比例(%)
     */
    private BigDecimal declarationRatio;
    /**
     * 类目佣金率(%)
     */
    private BigDecimal categoryCommissionRate;
    /**
     * 数字服务费率(%)（英国等）
     */
    private BigDecimal digitalServiceRate;
    /**
     * 是否启用FBA：1-是，0-否
     */
    private Integer fbaEnabled;
    /**
     * 广告费率(%)
     */
    private BigDecimal adRate;
    /**
     * 退货率(%)
     */
    private BigDecimal returnRate;
    /**
     * 海运计费方式：1-按体积，2-按重量
     */
    private Integer shippingCalculationType;
    /**
     * 海运单价
     */
    private BigDecimal shippingUnitPrice;
    /**
     * 默认币种代码
     */
    private String currencyCode;

}