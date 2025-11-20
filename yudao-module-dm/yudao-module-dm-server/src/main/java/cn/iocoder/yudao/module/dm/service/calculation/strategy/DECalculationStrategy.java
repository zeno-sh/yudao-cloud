package cn.iocoder.yudao.module.dm.service.calculation.strategy;

import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import cn.iocoder.yudao.module.dm.service.calculation.dto.ProfitCalculationResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 德国站点利润计算策略
 * <p>
 * 德国站点特点：
 * 1. 关税计算：基于申报价值和关税率
 * 2. VAT税计算：基于商品价值+关税+运费的总和
 * 3. 欧盟内部贸易规则
 * 4. 严格的税务合规要求
 *
 * @author Jax
 */
@Slf4j
@Component
public class DECalculationStrategy extends AbstractCountryCalculationStrategy {

    private static final String COUNTRY_CODE = "DE";
    private static final String COUNTRY_NAME = "德国";

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

        // 2. VAT税计算（德国标准VAT税率19%）
        calculateVAT(request, template, result);

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
     * 计算VAT税
     * VAT税基 = 商品价值 + 关税 + 运费
     * VAT税 = VAT税基 × VAT税率
     */
    private void calculateVAT(ProfitCalculationSaveReqVO request,
                              ProfitCalculationTemplateDO template,
                              ProfitCalculationResultDTO result) {
        // 获取VAT税率
        BigDecimal vatRate = request.getVatRate() != null ? request.getVatRate() : BigDecimal.valueOf(19); // 德国默认19%

        if (vatRate.compareTo(BigDecimal.ZERO) <= 0) {
            result.setVatCost(BigDecimal.ZERO);
            return;
        }

        // 获取申报比例
        BigDecimal declarationRatio = request.getDeclarationRatio() != null
                ? request.getDeclarationRatio().divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING)
                : BigDecimal.valueOf(0.7); // 默认70%申报

        // 计算VAT税基
        // VAT税基 = 申报价值 + 关税 + 运费（头程运费）
        BigDecimal declaredValue = result.getPurchaseCost()
                .multiply(declarationRatio);

        BigDecimal vatBase = declaredValue
                .add(result.getTariffCost() != null ? result.getTariffCost() : BigDecimal.ZERO)
                .add(result.getFirstMileFreightCost() != null ? result.getFirstMileFreightCost() : BigDecimal.ZERO);

        // 计算VAT税
        BigDecimal vatCost = vatBase
                .multiply(vatRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);

        result.setVatCost(vatCost);
    }

    /**
     * 计算数字服务费
     * 德国可能对数字服务征收特定费用
     */
    private void calculateDigitalServiceFee(ProfitCalculationSaveReqVO request,
                                            ProfitCalculationTemplateDO template,
                                            ProfitCalculationResultDTO result) {
        // 获取数字服务费率
        BigDecimal digitalServiceRate = request.getDigitalServiceRate();
        if (digitalServiceRate == null || digitalServiceRate.compareTo(BigDecimal.ZERO) <= 0) {
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

        // 德国站点特有的其他费用计算
        calculateEUComplianceFees(result);
    }

    /**
     * 计算欧盟合规费用
     * 包括WEEE费用、包装法费用等
     */
    private void calculateEUComplianceFees(ProfitCalculationResultDTO result) {

        // 示例：WEEE费用（电子产品回收费）
        BigDecimal weeeFee = BigDecimal.ZERO;

        // 将合规费用加入到其他费用中
        BigDecimal currentStorageCost = result.getStorageCost() != null ? result.getStorageCost() : BigDecimal.ZERO;
        BigDecimal totalOtherCost = currentStorageCost.add(weeeFee);
        result.setStorageCost(totalOtherCost);
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
                "2. VAT税计算：基于商品价值+关税+运费，标准税率19%%\n" +
                "3. 支持数字服务费计算\n" +
                "4. 包含欧盟合规费用（WEEE、包装法等）\n" +
                "5. 严格遵循德国税务法规", COUNTRY_NAME);
    }

    @Override
    public boolean validateRequest(ProfitCalculationSaveReqVO request, ProfitCalculationTemplateDO template) {

        return true;
    }
}