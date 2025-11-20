package cn.iocoder.yudao.module.dm.service.calculation.strategy;

import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import cn.iocoder.yudao.module.dm.service.calculation.dto.ProfitCalculationResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 美国站点利润计算策略
 * 
 * 美国站点特点：
 * 1. 关税计算：基于申报价值和关税率
 * 2. 无VAT税（美国不征收增值税）
 * 3. 可能有州销售税（暂不计算，因各州不同）
 * 4. FBA费用计算（如果启用）
 *
 * @author Jax
 */
@Slf4j
@Component
public class USCalculationStrategy extends AbstractCountryCalculationStrategy {

    private static final String COUNTRY_CODE = "US";
    private static final String COUNTRY_NAME = "美国";
    
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
        
        // 2. 美国无VAT税
        result.setVatCost(BigDecimal.ZERO);
        
        // 3. 数字服务费（如果有）
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
        BigDecimal declarationRatio = request.getDeclarationRatio() != null ? request.getDeclarationRatio() : BigDecimal.valueOf(100);
        
        // 计算申报价值
        BigDecimal declaredValue = result.getPurchaseCost()
                .multiply(declarationRatio.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING));
        
        // 计算关税
        BigDecimal tariffCost = declaredValue
                .multiply(tariffRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        result.setTariffCost(tariffCost);
    }

    /**
     * 计算数字服务费
     * 美国某些平台可能收取数字服务费
     */
    private void calculateDigitalServiceFee(ProfitCalculationSaveReqVO request,
                                             ProfitCalculationTemplateDO template,
                                             ProfitCalculationResultDTO result) {
        // 获取数字服务费率
        BigDecimal digitalServiceRate = request.getDigitalServiceRate() != null ? request.getDigitalServiceRate() : BigDecimal.ZERO;
        
        if (digitalServiceRate.compareTo(BigDecimal.ZERO) <= 0) {
            result.setDigitalServiceCost(BigDecimal.ZERO);
            return;
        }
        
        // 数字服务费 = 售价 × 数字服务费率
        BigDecimal digitalServiceCost = result.getPrice()
                .multiply(digitalServiceRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        result.setDigitalServiceCost(digitalServiceCost);
    }

    @Override
    protected void calculateOtherCosts(ProfitCalculationSaveReqVO request,
                                        ProfitCalculationTemplateDO template,
                                        ProfitCalculationResultDTO result) {
        super.calculateOtherCosts(request, template, result);
        
        // 美国站点特有的其他费用计算
        // 例如：FBA费用、长期仓储费等
        calculateFBAFees(request, template, result);
    }

    /**
     * 计算FBA费用（如果启用）
     */
    private void calculateFBAFees(ProfitCalculationSaveReqVO request,
                                   ProfitCalculationTemplateDO template,
                                   ProfitCalculationResultDTO result) {
        // 检查是否启用FBA
        Integer fbaEnabled = request.getFbaEnabled() != null ? request.getFbaEnabled() : 0;
        
        if (fbaEnabled == 0) {
            return; // 未启用FBA，不计算FBA费用
        }
        
        // FBA费用计算逻辑（根据产品尺寸和重量）
        // 这里可以根据亚马逊FBA费用标准进行计算
        // 暂时使用简化计算：基于重量的固定费率
        BigDecimal fbaFee = result.getCalculateWeight()
                .multiply(BigDecimal.valueOf(2.0)) // 假设每公斤2美元的FBA费用
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        // 将FBA费用加入到配送费中
        BigDecimal totalDeliveryCost = result.getDeliveryCost().add(fbaFee);
        result.setDeliveryCost(totalDeliveryCost);
    }

    @Override
    protected void generateConfigSnapshot(ProfitCalculationSaveReqVO request,
                                          ProfitCalculationTemplateDO template,
                                          BigDecimal exchangeRate,
                                          ProfitCalculationResultDTO result) {
    }

    @Override
    public String getCalculationDescription() {
        return String.format("%s站点利润计算策略：\n" +
                "1. 关税计算：基于申报价值（采购成本×申报比例）和关税率\n" +
                "2. 无VAT税（美国不征收增值税）\n" +
                "3. 支持FBA费用计算（如果启用）\n" +
                "4. 支持数字服务费计算\n" +
                "5. 各州销售税因地而异，暂不计算", COUNTRY_NAME);
    }

    @Override
    public boolean validateRequest(ProfitCalculationSaveReqVO request, ProfitCalculationTemplateDO template) {
        // 调用父类基础验证
        return super.validateRequest(request, template);
    }
}