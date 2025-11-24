package cn.iocoder.yudao.module.dm.service.calculation.strategy;

import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import cn.iocoder.yudao.module.dm.service.calculation.dto.ProfitCalculationResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 俄罗斯站点利润计算策略
 * 
 * 俄罗斯站点特点：
 * 1. 没有数字服务费
 * 2. 进口VAT固定20%
 * 3. 销售税20%
 * 4. 补缴VAT=销售VAT-进口VAT-(佣金+物流配送费+广告)/1.2*20%
 * 5. 销售VAT=售价/1.2*20%
 * 
 * @author Jax
 */
@Slf4j
@Component
public class RUCalculationStrategy extends AbstractCountryCalculationStrategy {

    private static final String COUNTRY_CODE = "RU";
    private static final String COUNTRY_NAME = "俄罗斯";
    
    // 俄罗斯固定税率
    private static final BigDecimal IMPORT_VAT_RATE = BigDecimal.valueOf(20); // 进口VAT 20%
    private static final BigDecimal VAT_DIVISOR = BigDecimal.valueOf(1.2);    // VAT除数 1.2
    
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
        
        // 2. 进口VAT和销售VAT计算
        calculateVAT(request, template, result);
        
        // 3. 补缴VAT计算(销售税)
        calculateSaleVAT(request, template, result);
        
        // 4. 数字服务费为0
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
                : BigDecimal.valueOf(1); 
        
        // 计算申报价值 = 采购成本 × 申报比例
        BigDecimal declaredValue = result.getPurchaseCost()
                .multiply(declarationRatio);
        
        // 计算关税
        BigDecimal tariffCost = declaredValue
                .multiply(tariffRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        result.setTariffCost(tariffCost);
    }

    /**
     * 计算进口VAT
     * 进口VAT固定20%
     * 进口VAT税基 = 申报价值 + 关税
     * 举例：申报价值100元，关税率10%，则进口VAT = (100 + 100*10%) * 20%
     */
    private void calculateVAT(ProfitCalculationSaveReqVO request,
                              ProfitCalculationTemplateDO template,
                              ProfitCalculationResultDTO result) {
        // 获取申报比例
        BigDecimal declarationRatio = request.getDeclarationRatio() != null 
                ? request.getDeclarationRatio().divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING)
                : BigDecimal.valueOf(1);
        
        // 计算申报价值 = 采购成本 × 申报比例
        BigDecimal declaredValue = result.getPurchaseCost()
                .multiply(declarationRatio);
        
        // 计算进口VAT税基 = 申报价值 + 关税（不包括运费）
        BigDecimal importVatBase = declaredValue
                .add(result.getTariffCost() != null ? result.getTariffCost() : BigDecimal.ZERO);
        
        // 计算进口VAT：固定20%
        BigDecimal importVatCost = importVatBase
                .multiply(IMPORT_VAT_RATE.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        result.setVatCost(importVatCost);
    }

    /**
     * 计算补缴VAT(销售税)
     * 销售VAT = 售价/1.2*20%
     * 补缴VAT = 销售VAT - 进口VAT - (佣金+物流配送费+广告)/1.2*20%
     */
    private void calculateSaleVAT(ProfitCalculationSaveReqVO request,
                                  ProfitCalculationTemplateDO template,
                                  ProfitCalculationResultDTO result) {

        BigDecimal saleVatRate = request.getSaleRate() == null ? BigDecimal.ZERO : request.getSaleRate();

        // 计算销售VAT = 售价/1.2*20%
        BigDecimal saleVAT = result.getPrice()
                .divide(VAT_DIVISOR, SCALE_MONEY, DEFAULT_ROUNDING)
                .multiply(saleVatRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        // 获取进口VAT
        BigDecimal importVAT = result.getVatCost() != null ? result.getVatCost() : BigDecimal.ZERO;
        
        // 获取佣金、物流配送费、广告费
        BigDecimal commission = result.getCategoryCommissionCost() != null ? result.getCategoryCommissionCost() : BigDecimal.ZERO;
        BigDecimal deliveryCost = result.getDeliveryCost() != null ? result.getDeliveryCost() : BigDecimal.ZERO;
        BigDecimal adCost = result.getAdCost() != null ? result.getAdCost() : BigDecimal.ZERO;
        
        // 计算可抵扣部分 = (佣金+物流配送费+广告)/1.2*20%
        BigDecimal deductibleAmount = commission.add(deliveryCost).add(adCost)
                .divide(VAT_DIVISOR, SCALE_MONEY, DEFAULT_ROUNDING)
                .multiply(saleVatRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        // 计算补缴VAT = 销售VAT - 进口VAT - 可抵扣部分
        BigDecimal actualTaxAmount = saleVAT.subtract(importVAT).subtract(deductibleAmount)
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        
        // 如果补缴VAT为负数，设置为0
        if (actualTaxAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualTaxAmount = BigDecimal.ZERO;
        }
        
        result.setSaleCost(saleVAT);
        result.setActualTaxAmount(actualTaxAmount);
    }

    @Override
    protected void calculateOtherCosts(ProfitCalculationSaveReqVO request,
                                       ProfitCalculationTemplateDO template,
                                       ProfitCalculationResultDTO result) {
        super.calculateOtherCosts(request, template, result);
        // 俄罗斯站点没有特殊的其他费用
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
            "   - 关税 = 申报价值 × 关税率（申报价值=采购成本×申报比例）\n" +
            "   - 进口VAT = (申报价值+关税) × 20%%（固定，不含运费）\n" +
            "   - 销售VAT = 售价 ÷ 1.2 × 20%%\n" +
            "   - 补缴VAT = 销售VAT - 进口VAT - (佣金+配送费+广告费) ÷ 1.2 × 20%%\n" +
            "   - 海运费 = 海运单价 × 实际体积(或重量) ÷ 汇率\n" +
            "   - 亚马逊抽成 = 售价 × 亚马逊佣金抽成\n" +
            "   - 数字服务费 = 0（俄罗斯无数字服务费）\n" +
            "   - 广告费用 = 售价 × 自定义百分比\n" +
            "   - 退换货费用 = (采购成本+运费+税费+配送费+仓储+抽成×20%%) × 退货率\n" +
            "   - 举例：申报价值100元，关税率10%%，进口VAT=(100+100×10%%)×20%%=22元\n" +
            "\n" +
            "3. 利润计算：\n" +
            "   - 利润 = 售价 - 采购成本 - 国内运费 - 货代费 - 关税 - 进口VAT - 补缴VAT - 海运费 - 配送费 - 仓储费 - 亚马逊抽成 - 广告 - 退换货\n" +
            "   - 利润率 = 利润 ÷ 售价\n" +
            "   - 投资回报率 = 利润 ÷ 采购成本\n",
            COUNTRY_NAME
        );
    }

    @Override
    protected void generateConfigSnapshot(ProfitCalculationSaveReqVO request,
                                          ProfitCalculationTemplateDO template,
                                          BigDecimal exchangeRate,
                                          ProfitCalculationResultDTO result) {
        // 俄罗斯站点暂不需要生成配置快照
    }
}
