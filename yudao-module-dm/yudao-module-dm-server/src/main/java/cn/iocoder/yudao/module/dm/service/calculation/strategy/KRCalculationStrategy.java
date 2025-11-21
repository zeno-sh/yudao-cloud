package cn.iocoder.yudao.module.dm.service.calculation.strategy;

import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import cn.iocoder.yudao.module.dm.service.calculation.dto.ProfitCalculationResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 韩国站点利润计算策略
 * 
 * 韩国站点特点：
 * 1. 有关税（分包税和自税）
 * 2. 有销售附加税（增值税）：10%固定税率
 * 3. 销售税计算：实际应纳税额 = 销项税 - 进项税
 *    - 销项税 = 售价 × 10%
 *    - 进项税 = 采购成本 × 10%
 * 4. 其他计算逻辑与其他国家相同
 * 
 * @author Jax
 */
@Slf4j
@Component
public class KRCalculationStrategy extends AbstractCountryCalculationStrategy {

    private static final String COUNTRY_CODE = "KR";
    private static final String COUNTRY_NAME = "韩国";
    
    @Override
    public String getSupportedCountry() {
        return COUNTRY_NAME;
    }

    @Override
    protected void calculateTaxCosts(ProfitCalculationSaveReqVO request,
                                     ProfitCalculationTemplateDO template,
                                     ProfitCalculationResultDTO result) {
        // 1. 关税计算
        calculateTariff(request, template, result);
        
        // 2. 进口增值税(VAT)计算
        calculateImportVAT(request, template, result);
        
        // 3. 销售附加税（增值税）计算
        calculateSalesTax(request, template, result);
        
        // 4. 韩国站点无数字服务费
        result.setDigitalServiceCost(BigDecimal.ZERO);
    }

    /**
     * 计算关税
     * 关税 = 申报价值 × 关税率
     * 申报价值 = 采购成本 × 申报比例
     */
    private void calculateTariff(ProfitCalculationSaveReqVO request,
                                 ProfitCalculationTemplateDO template,
                                 ProfitCalculationResultDTO result) {
        // 获取关税率
        BigDecimal tariffRate = request.getTariffRate() != null ? request.getTariffRate() : BigDecimal.ZERO;
        
        if (tariffRate.compareTo(BigDecimal.ZERO) <= 0) {
            result.setTariffCost(BigDecimal.ZERO);
            return;
        }
        
        // 获取申报比例
        BigDecimal declarationRatio = request.getDeclarationRatio() != null 
                ? request.getDeclarationRatio().divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING)
                : BigDecimal.valueOf(0.7); // 默认70%申报
        
        BigDecimal dutyableValue = result.getPurchaseCost()
                .multiply(declarationRatio)
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        BigDecimal tariffCost = dutyableValue
                .multiply(tariffRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        result.setTariffCost(tariffCost);
    }

    /**
     * 计算进口增值税(VAT)
     * 韩国进口增值税固定为10%
     * 
     * 计算步骤：
     * 1. 采购成本(韩币) = 采购单价(CNY) ÷ 汇率 (已在initializeResult中计算)
     * 2. VAT(进项税) = 采购成本(韩币) × 申报货值比例 × 10%
     */
    private void calculateImportVAT(ProfitCalculationSaveReqVO request,
                                    ProfitCalculationTemplateDO template,
                                    ProfitCalculationResultDTO result) {
        // 获取VAT税率（默认10%）
        BigDecimal vatRate = request.getVatRate() != null ? request.getVatRate() : BigDecimal.valueOf(10);
        
        if (vatRate.compareTo(BigDecimal.ZERO) <= 0) {
            result.setVatCost(BigDecimal.ZERO);
            return;
        }
        
        // 获取申报比例
        BigDecimal declarationRatio = request.getDeclarationRatio() != null 
                ? request.getDeclarationRatio().divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING)
                : BigDecimal.valueOf(0.7); // 默认70%申报
        
        // 采购成本(韩币)已在initializeResult中计算：purchaseUnit(CNY) ÷ exchangeRate
        BigDecimal purchaseCostKRW = result.getPurchaseCost();
        
        // VAT(进项税) = 采购成本(韩币) × 申报货值比例 × 10%
        BigDecimal vatCost = purchaseCostKRW
                .multiply(declarationRatio)
                .multiply(vatRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        result.setVatCost(vatCost);
        
        log.debug("韩国进口增值税计算 - 采购单价(CNY): {}, 汇率: {}, 采购成本(韩币): {}, 申报比例: {}%, VAT税率: {}%, VAT费用(进项税): {}", 
                result.getPurchaseUnit(), result.getExchangeRate(), purchaseCostKRW,
                declarationRatio.multiply(BigDecimal.valueOf(100)), vatRate, vatCost);
    }

    /**
     * 计算销售附加税（增值税）
     * 韩国增值税为价内税，税率固定为10%
     * 
     * 计算逻辑：
     * 1. 不含税销售额 = 含税售价 ÷ (1 + 10%) 
     * 2. 销项税 = 不含税销售额 × 10% （或 含税售价 - 不含税销售额）
     * 3. 进项税 = 进口增值税(VAT)，已在calculateImportVAT中计算
     * 4. 实际应纳税额 = 销项税 - 进项税
     * 5. saleCost存储销项税，actualTaxAmount存储实际应纳税额
     */
    private void calculateSalesTax(ProfitCalculationSaveReqVO request,
                                    ProfitCalculationTemplateDO template,
                                    ProfitCalculationResultDTO result) {
        // 获取销售税率（默认10%）
        BigDecimal saleRate = request.getSaleRate() != null ? request.getSaleRate() : BigDecimal.valueOf(10);
        
        if (saleRate.compareTo(BigDecimal.ZERO) <= 0) {
            result.setSaleCost(BigDecimal.ZERO);
            result.setActualTaxAmount(BigDecimal.ZERO);
            return;
        }
        
        // 计算不含税销售额：含税售价 ÷ (1 + 10%)
        BigDecimal taxRateDecimal = saleRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING);
        BigDecimal priceExcludingTax = result.getPrice()
                .divide(BigDecimal.ONE.add(taxRateDecimal), SCALE_MONEY, DEFAULT_ROUNDING);
        
        // 计算销项税：不含税销售额 × 10% (或直接用 含税售价 - 不含税销售额)
        BigDecimal outputTax = priceExcludingTax
                .multiply(taxRateDecimal)
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        // 进项税就是进口增值税(VAT)
        BigDecimal inputTax = result.getVatCost() != null ? result.getVatCost() : BigDecimal.ZERO;
        
        // 实际应纳税额 = 销项税 - 进项税
        BigDecimal actualTaxAmount = outputTax.subtract(inputTax)
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        // 存储销项税到saleCost（这是含在售价中的增值税）
        result.setSaleCost(outputTax);
        // 存储实际应纳税额（这是企业实际需要缴纳的税额）
        result.setActualTaxAmount(actualTaxAmount);
        
        log.debug("韩国销售税计算 - 含税售价: {}, 不含税销售额: {}, 销项税: {}, 进项税(VAT): {}, 实际应纳税额: {}", 
                result.getPrice(), priceExcludingTax, outputTax, inputTax, actualTaxAmount);
    }

    @Override
    protected void calculateOtherCosts(ProfitCalculationSaveReqVO request,
                                        ProfitCalculationTemplateDO template,
                                        ProfitCalculationResultDTO result) {
        super.calculateOtherCosts(request, template, result);
        
        // 韩国站点退换货费用计算不包含数字服务费（因为没有数字服务费）
        calculateReturnCostWithoutDigitalService(request, template, result);
    }

    /**
     * 计算不包含数字服务费的退换货费用
     * 退换货费用 = (采购成本+国内运费+货代费+关税+销售税+海运费+配送费+仓储+佣金)*退货率
     */
    private void calculateReturnCostWithoutDigitalService(ProfitCalculationSaveReqVO request,
                                                           ProfitCalculationTemplateDO template,
                                                           ProfitCalculationResultDTO result) {
        BigDecimal returnRate = request.getReturnRate();
        if (returnRate == null || returnRate.compareTo(BigDecimal.ZERO) <= 0) {
            result.setReturnCost(BigDecimal.ZERO);
            return;
        }
        
        // 计算退换货费用基数（不包含数字服务费）
        BigDecimal returnBase = BigDecimal.ZERO
                .add(result.getPurchaseCost())
                .add(result.getLocalTransportCost())
                .add(result.getFreightForwarderCost())
                .add(result.getTariffCost())
                .add(result.getSaleCost() != null ? result.getSaleCost() : BigDecimal.ZERO) // 销售税成本
                .add(result.getFirstMileFreightCost())
                .add(result.getDeliveryCost())
                .add(result.getStorageCost())
                .add(result.getCategoryCommissionCost());
        
        BigDecimal returnCost = returnBase
                .multiply(returnRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        result.setReturnCost(returnCost);
    }

    @Override
    public boolean validateRequest(ProfitCalculationSaveReqVO request, ProfitCalculationTemplateDO template) {
        // 调用父类基础验证
        return super.validateRequest(request, template);
    }

    @Override
    public String getCalculationDescription() {
        return String.format(
            "【%s站点利润计算说明】\n" +
            "1. 基础计算：\n" +
            "   - 立方米 = 长cm × 宽cm × 高cm × 0.000001\n" +
            "   - 计算重量 = 立方米 × 体积系数\n" +
            "   - 计算体积 = KGS ÷ 重量系数\n" +
            "   - 实际体积 = max(立方米, 计算体积)\n" +
            "   - 实际重量 = max(KGS, 计算重量)\n" +
            "\n" +
            "2. 费用计算：\n" +
            "   - 采购成本 = 成本 ÷ 汇率\n" +
            "   - 国内运费 = 国内运费单价 × 立方米 ÷ 汇率\n" +
            "   - 货代费 = 货代费用单价 × 立方米 ÷ 汇率\n" +
            "   - 关税 = 采购成本 × 申报比例 × 关税率\n" +
            "   - 销售附加税（增值税，10%）：\n" +
            "     * 销项税 = 售价 × 10%%\n" +
            "     * 进项税 = 采购成本 × 10%%\n" +
            "     * 实际应纳税额 = 销项税 - 进项税\n" +
            "   - 海运费 = 海运单价 × 实际体积(或重量) ÷ 汇率\n" +
            "   - 类目佣金 = 售价 × 佣金率\n" +
            "   - 数字服务费 = 0（韩国站点无数字服务费）\n" +
            "   - 广告费用 = 售价 × 广告费率\n" +
            "   - 退换货费用 = (采购成本+运费+税费+配送费+仓储+抽成×20%%) × 退货率\n" +
            "\n" +
            "3. 利润计算：\n" +
            "   - 总成本 = 采购成本 + 国内运费 + 货代费 + 关税 + 销售税 + 海运费 + 配送费 + 仓储费 + 类目佣金 + 广告 + 退换货\n" +
            "   - 毛利润 = 售价 - 总成本\n" +
            "   - 毛利率 = 毛利润 ÷ 售价 × 100%%\n" +
            "   - 投资回报率 = 毛利润 ÷ 采购成本 × 100%%\n" +
            "\n" +
            "注意：\n" +
            "   - 韩国增值税为价外税，税率固定为10%%\n" +
            "   - 增值税由企业代收代缴，最终由消费者承担\n" +
            "   - 销售税计算考虑了进项税抵扣机制\n" +
            "   - 韩国站点无数字服务费\n",
            COUNTRY_NAME
        );
    }

    @Override
    protected void generateConfigSnapshot(ProfitCalculationSaveReqVO request,
                                          ProfitCalculationTemplateDO template,
                                          BigDecimal exchangeRate,
                                          ProfitCalculationResultDTO result) {
        // 配置快照生成逻辑（可选实现）
    }
}
