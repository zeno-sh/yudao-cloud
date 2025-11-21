package cn.iocoder.yudao.module.dm.service.calculation.strategy;

import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import cn.iocoder.yudao.module.dm.service.calculation.dto.ProfitCalculationResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 加拿大站点利润计算策略
 * 
 * 加拿大站点特点：
 * 1. 有关税（分包税和自税）
 * 2. 有增值税：售价-售价/(1+增值税率)
 * 3. 有数字服务费：(FBA配送费+亚马逊抽成)*3%（仅FBA配送）
 * 4. 海运费公式待定
 * 5. 退换货费用计算包含数字服务费
 * 
 * @author Jax
 */
@Slf4j
@Component
public class CACalculationStrategy extends AbstractCountryCalculationStrategy {

    private static final String COUNTRY_CODE = "CA";
    private static final String COUNTRY_NAME = "加拿大";
    
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
        
        // 2. 增值税计算（加拿大GST/HST）
        calculateVAT(request, template, result);
        
        // 3. 数字服务费（仅FBA配送）
        calculateDigitalServiceFee(request, template, result);
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
     * 计算增值税（GST/HST）
     * 加拿大增值税计算公式：售价-售价/(1+增值税率)
     */
    private void calculateVAT(ProfitCalculationSaveReqVO request,
                              ProfitCalculationTemplateDO template,
                              ProfitCalculationResultDTO result) {
        // 获取增值税率
        BigDecimal vatRate = request.getVatRate() != null ? request.getVatRate() : BigDecimal.ZERO;
        
        if (vatRate.compareTo(BigDecimal.ZERO) <= 0) {
            result.setVatCost(BigDecimal.ZERO);
            return;
        }
        
        // 加拿大增值税计算：售价-售价/(1+增值税率)
        BigDecimal vatRateDecimal = vatRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING);
        BigDecimal divisor = BigDecimal.ONE.add(vatRateDecimal);
        
        BigDecimal vatCost = result.getPrice()
                .subtract(result.getPrice().divide(divisor, SCALE_MONEY, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        result.setVatCost(vatCost);
    }

    /**
     * 计算数字服务费
     * 加拿大数字服务费：(FBA配送费+亚马逊抽成)*3%（仅FBA配送才有）
     */
    private void calculateDigitalServiceFee(ProfitCalculationSaveReqVO request,
                                             ProfitCalculationTemplateDO template,
                                             ProfitCalculationResultDTO result) {
        // 检查是否启用FBA
        if (request.getFbaEnabled() == null || request.getFbaEnabled() != 1) {
            result.setDigitalServiceCost(BigDecimal.ZERO);
            return;
        }
        
        // 获取数字服务费率，默认3%
        BigDecimal digitalServiceRate = request.getDigitalServiceRate();
        if (digitalServiceRate == null) {
            digitalServiceRate = BigDecimal.valueOf(3.00); // 默认3%
        }
        
        if (digitalServiceRate.compareTo(BigDecimal.ZERO) <= 0) {
            result.setDigitalServiceCost(BigDecimal.ZERO);
            return;
        }
        
        // 计算数字服务费：(FBA配送费+亚马逊抽成)*3%
        BigDecimal fbaDeliveryCost = result.getDeliveryCost() != null ? result.getDeliveryCost() : BigDecimal.ZERO;
        BigDecimal amazonCommission = result.getCategoryCommissionCost() != null ? result.getCategoryCommissionCost() : BigDecimal.ZERO;
        
        BigDecimal digitalServiceCost = fbaDeliveryCost.add(amazonCommission)
                .multiply(digitalServiceRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        result.setDigitalServiceCost(digitalServiceCost);
    }

    @Override
    protected void calculateOtherCosts(ProfitCalculationSaveReqVO request,
                                        ProfitCalculationTemplateDO template,
                                        ProfitCalculationResultDTO result) {
        super.calculateOtherCosts(request, template, result);
        
        // 加拿大站点退换货费用计算包含数字服务费
        calculateReturnCostWithDigitalService(request, template, result);
    }

    /**
     * 计算包含数字服务费的退换货费用
     * 退换货费用 = (采购成本+国内运费+货代费+关税+VAT+海运费+配送费+仓储+亚马逊抽成*20%+数字服务费)*退货率
     */
    private void calculateReturnCostWithDigitalService(ProfitCalculationSaveReqVO request,
                                                        ProfitCalculationTemplateDO template,
                                                        ProfitCalculationResultDTO result) {
        BigDecimal returnRate = request.getReturnRate();
        if (returnRate == null || returnRate.compareTo(BigDecimal.ZERO) <= 0) {
            result.setReturnCost(BigDecimal.ZERO);
            return;
        }
        
        // 计算退换货费用基数
        BigDecimal returnBase = BigDecimal.ZERO
                .add(result.getPurchaseCost() != null ? result.getPurchaseCost() : BigDecimal.ZERO)
                .add(result.getLocalTransportCost() != null ? result.getLocalTransportCost() : BigDecimal.ZERO)
                .add(result.getFreightForwarderCost() != null ? result.getFreightForwarderCost() : BigDecimal.ZERO)
                .add(result.getTariffCost() != null ? result.getTariffCost() : BigDecimal.ZERO)
                .add(result.getVatCost() != null ? result.getVatCost() : BigDecimal.ZERO)
                .add(result.getFirstMileFreightCost() != null ? result.getFirstMileFreightCost() : BigDecimal.ZERO)
                .add(result.getDeliveryCost() != null ? result.getDeliveryCost() : BigDecimal.ZERO)
                .add(result.getStorageCost() != null ? result.getStorageCost() : BigDecimal.ZERO)
                .add(result.getCategoryCommissionCost() != null ? result.getCategoryCommissionCost() : BigDecimal.ZERO)
                .add(result.getDigitalServiceCost() != null ? result.getDigitalServiceCost() : BigDecimal.ZERO); // 包含数字服务费
        
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
            "   - 计算重量 = 立方米 × 167（货代提供系数）\n" +
            "   - 计算体积 = KGS ÷ 400（货代提供系数）\n" +
            "   - 实际体积 = max(立方米, 计算体积)\n" +
            "   - 实际重量 = max(KGS, 计算重量)\n" +
            "\n" +
            "2. 费用计算：\n" +
            "   - 采购成本 = 成本 ÷ 汇率\n" +
            "   - 国内运费 = 150 × 立方米 ÷ 汇率\n" +
            "   - 货代费 = 60 × 立方米 ÷ 汇率\n" +
            "   - 关税 = 采购成本 × 0.3 × 税率\n" +
            "   - 增值税(GST/HST) = 售价 - 售价÷(1+增值税率)\n" +
            "   - 海运费 = 海运单价 × 实际体积(或重量) ÷ 汇率（公式待定）\n" +
            "   - 亚马逊抽成 = 售价 × 亚马逊佣金抽成\n" +
            "   - 数字服务费 = (FBA配送费+亚马逊抽成) × 3%（仅FBA）\n" +
            "   - 广告费用 = 售价 × 自定义百分比\n" +
            "   - 退换货费用 = (采购成本+运费+税费+配送费+仓储+抽成×20%+数字服务费) × 退货率\n" +
            "\n" +
            "3. 利润计算：\n" +
            "   - 利润 = 售价 - 采购成本 - 国内运费 - 货代费 - 关税 - 增值税 - 海运费 - 配送费 - 仓储费 - 亚马逊抽成 - 数字服务费 - 广告 - 退换货 + 退税\n" +
            "   - 利润率 = 利润 ÷ 售价\n" +
            "   - 投资回报率 = 利润 ÷ (采购成本+国内运费+货代费+关税+增值税+海运费)\n" +
            "\n" +
            "注意：海运费计算公式待定制化\n",
            COUNTRY_NAME
        );
    }

    @Override
    protected void generateConfigSnapshot(ProfitCalculationSaveReqVO request,
                                          ProfitCalculationTemplateDO template,
                                          BigDecimal exchangeRate,
                                          ProfitCalculationResultDTO result) {
    }
}