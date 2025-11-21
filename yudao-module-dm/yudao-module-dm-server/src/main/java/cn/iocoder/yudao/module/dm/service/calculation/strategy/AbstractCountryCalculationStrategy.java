package cn.iocoder.yudao.module.dm.service.calculation.strategy;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import cn.iocoder.yudao.module.dm.service.calculation.dto.ProfitCalculationResultDTO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 抽象国家计算策略基类
 * <p>
 * 遵循模板方法模式：定义计算的基本流程，子类实现特定的计算细节
 * 遵循DRY原则：将通用计算逻辑抽取到基类中，避免重复代码
 * 遵循单一职责原则：基类负责通用逻辑，子类负责特定国家逻辑
 *
 * @author Jax
 */
@Slf4j
public abstract class AbstractCountryCalculationStrategy implements CountryCalculationStrategy {

    // 计算精度常量
    protected static final int SCALE_MONEY = 2;  // 金额精度：2位小数
    protected static final int SCALE_RATE = 4;   // 费率精度：4位小数
    protected static final int SCALE_WEIGHT = 3; // 重量精度：3位小数
    protected static final int SCALE_VOLUME = 6; // 体积精度：6位小数

    // 默认舍入模式
    protected static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    @Override
    public ProfitCalculationResultDTO calculate(ProfitCalculationSaveReqVO request,
                                                ProfitCalculationTemplateDO template,
                                                BigDecimal exchangeRate) {

        // 2. 初始化结果对象
        ProfitCalculationResultDTO result = initializeResult(request, template, exchangeRate);

        // 3. 基础计算（体积、重量等）
        calculateBasicMetrics(request, template, result);

        // 4. 运费计算
        calculateFreightCosts(request, template, result);

        // 5. 税费计算（国家特定）
        calculateTaxCosts(request, template, result);

        // 6. 平台费用计算
        calculatePlatformCosts(request, template, result);

        // 7. 其他费用计算
        calculateOtherCosts(request, template, result);

        // 8. 最终利润计算
        calculateFinalProfit(request, template, result);

        return result;
    }

    /**
     * 初始化结果对象
     */
    protected ProfitCalculationResultDTO initializeResult(ProfitCalculationSaveReqVO request,
                                                          ProfitCalculationTemplateDO template,
                                                          BigDecimal exchangeRate) {

        // 计算采购成本：采购单价(CNY) / 汇率 = 采购成本(目标币种)
        BigDecimal purchaseCost = BigDecimal.ZERO;
        if (request.getPurchaseUnit() != null && exchangeRate != null && exchangeRate.compareTo(BigDecimal.ZERO) > 0) {
            purchaseCost = request.getPurchaseUnit().divide(exchangeRate, SCALE_MONEY, DEFAULT_ROUNDING);
        }

        ProfitCalculationResultDTO result = BeanUtils.toBean(request, ProfitCalculationResultDTO.class);
        result.setTemplateId(template != null ? template.getId() : null);
        result.setPurchaseCost(purchaseCost);

        return result;
    }

    /**
     * 基础指标计算（体积、重量等）
     */
    protected void calculateBasicMetrics(ProfitCalculationSaveReqVO request,
                                         ProfitCalculationTemplateDO template,
                                         ProfitCalculationResultDTO result) {
        // 体积（立方米）
        BigDecimal volumeM3 = request.getProductLength()
                .multiply(request.getProductWidth())
                .multiply(request.getProductHeight())
                .divide(BigDecimal.valueOf(1000000), SCALE_VOLUME, DEFAULT_ROUNDING); // cm³ 转 m³
        result.setVolumeM3(volumeM3);

        BigDecimal volumeCoefficient = request.getVolumeCoefficient();
        BigDecimal weightCoefficient = request.getWeightCoefficient();


        // 计算重量=体积x体积系数（体积重量 vs 实际重量取大值）
        BigDecimal volumeWeight = volumeM3.multiply(volumeCoefficient);
        BigDecimal calculateWeight = volumeWeight.max(request.getProductWeight());
        result.setCalculateWeight(calculateWeight.setScale(SCALE_WEIGHT, DEFAULT_ROUNDING));

        // 计算体积（重量转体积）
        BigDecimal calculateVolume = request.getProductWeight().divide(weightCoefficient, SCALE_VOLUME, DEFAULT_ROUNDING);
        result.setCalculateVolume(calculateVolume);

        result.setActualVolume(volumeM3);
        result.setActualWeight(request.getProductWeight());
    }

    /**
     * 运费计算
     */
    protected void calculateFreightCosts(ProfitCalculationSaveReqVO request,
                                         ProfitCalculationTemplateDO template,
                                         ProfitCalculationResultDTO result) {
        // 获取国内运费单价
        BigDecimal domesticFreightUnit = request.getDomesticFreightUnit();

        // 国内运费 = 立方米 × 国内运费单价 / 汇率（CNY转目标币种）
        BigDecimal localTransportCost = result.getVolumeM3()
                .multiply(domesticFreightUnit)
                .divide(result.getExchangeRate(), SCALE_MONEY, DEFAULT_ROUNDING);
        result.setLocalTransportCost(localTransportCost);

        // 获取货代费用单价
        BigDecimal freightForwarderUnit = request.getFreightForwarderUnit();

        // 货代费用 = 立方米 × 货代费用单价 / 汇率（CNY转目标币种）
        BigDecimal freightForwarderCost = result.getVolumeM3()
                .multiply(freightForwarderUnit)
                .divide(result.getExchangeRate(), SCALE_MONEY, DEFAULT_ROUNDING);
        result.setFreightForwarderCost(freightForwarderCost);

        // 头程运费（海运费）
        BigDecimal firstMileFreightCost = calculateShippingCost(request, template, result);
        result.setFirstMileFreightCost(firstMileFreightCost);
    }

    /**
     * 海运费计算
     */
    protected BigDecimal calculateShippingCost(ProfitCalculationSaveReqVO request,
                                               ProfitCalculationTemplateDO template,
                                               ProfitCalculationResultDTO result) {
        // 获取海运单价
        BigDecimal shippingUnitPrice = request.getShippingUnitPrice();

        if (shippingUnitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 获取海运计费方式
        Integer shippingCalculationType = request.getShippingCalculationType();

        BigDecimal shippingCost;
        if (shippingCalculationType == 1) { // 按体积计费
            shippingCost = result.getCalculateVolume().multiply(shippingUnitPrice);
        } else { // 按重量计费
            shippingCost = result.getCalculateWeight().multiply(shippingUnitPrice);
        }

        // 海运费需要除以汇率（CNY转目标币种）
        return shippingCost.divide(result.getExchangeRate(), SCALE_MONEY, DEFAULT_ROUNDING);
    }

    /**
     * 税费计算（抽象方法，由子类实现国家特定逻辑）
     */
    protected abstract void calculateTaxCosts(ProfitCalculationSaveReqVO request,
                                              ProfitCalculationTemplateDO template,
                                              ProfitCalculationResultDTO result);

    /**
     * 平台费用计算
     */
    protected void calculatePlatformCosts(ProfitCalculationSaveReqVO request,
                                          ProfitCalculationTemplateDO template,
                                          ProfitCalculationResultDTO result) {
        // 获取类目佣金率
        BigDecimal categoryCommissionRate = request.getCategoryCommissionRate();

        // 类目佣金 = 售价 × 佣金率
        BigDecimal categoryCommissionCost = result.getPrice()
                .multiply(categoryCommissionRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        result.setCategoryCommissionCost(categoryCommissionCost);

        // 获取广告费率
        BigDecimal adRate = request.getAdRate() != null ? request.getAdRate() : BigDecimal.ZERO;

        // 广告费用 = 售价 × 广告费率
        BigDecimal adCost = result.getPrice()
                .multiply(adRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        result.setAdCost(adCost);

        // 获取退货率
        BigDecimal returnRate = request.getReturnRate() != null ? request.getReturnRate() : BigDecimal.ZERO;

        // 退换货费用 = (采购成本+国内运费+货代费+关税+海运费+配送费+仓储+亚马逊抽成*20%) × 退货率
        BigDecimal baseCosts = (result.getPurchaseCost() != null ? result.getPurchaseCost() : BigDecimal.ZERO)
                .add(result.getLocalTransportCost() != null ? result.getLocalTransportCost() : BigDecimal.ZERO)
                .add(result.getFreightForwarderCost() != null ? result.getFreightForwarderCost() : BigDecimal.ZERO)
                .add(result.getTariffCost() != null ? result.getTariffCost() : BigDecimal.ZERO)
                .add(result.getFirstMileFreightCost() != null ? result.getFirstMileFreightCost() : BigDecimal.ZERO)
                .add(result.getDeliveryCost() != null ? result.getDeliveryCost() : BigDecimal.ZERO)
                .add(result.getStorageCost() != null ? result.getStorageCost() : BigDecimal.ZERO)
                .add(result.getCategoryCommissionCost() != null ? result.getCategoryCommissionCost() : BigDecimal.ZERO); // 亚马逊抽成的20%

        BigDecimal returnCost = baseCosts
                .multiply(returnRate.divide(BigDecimal.valueOf(100), SCALE_RATE, DEFAULT_ROUNDING))
                .setScale(SCALE_MONEY, DEFAULT_ROUNDING);
        result.setReturnCost(returnCost);

    }

    /**
     * 其他费用计算
     */
    protected void calculateOtherCosts(ProfitCalculationSaveReqVO request,
                                       ProfitCalculationTemplateDO template,
                                       ProfitCalculationResultDTO result) {
        // 配送费和仓储费已在初始化时设置，这里可以进行额外处理
        // 子类可以重写此方法添加特定的其他费用计算
    }

    /**
     * 最终利润计算
     */
    protected void calculateFinalProfit(ProfitCalculationSaveReqVO request,
                                        ProfitCalculationTemplateDO template,
                                        ProfitCalculationResultDTO result) {
        // 总成本计算
        BigDecimal totalCost = BigDecimal.ZERO
                .add(result.getPurchaseCost() != null ? result.getPurchaseCost() : BigDecimal.ZERO)
                .add(result.getLocalTransportCost() != null ? result.getLocalTransportCost() : BigDecimal.ZERO)
                .add(result.getFreightForwarderCost() != null ? result.getFreightForwarderCost() : BigDecimal.ZERO)
                .add(result.getFirstMileFreightCost() != null ? result.getFirstMileFreightCost() : BigDecimal.ZERO)
                .add(result.getTariffCost() != null ? result.getTariffCost() : BigDecimal.ZERO)
                .add(result.getVatCost() != null ? result.getVatCost() : BigDecimal.ZERO)
                .add(result.getSaleCost() != null ? result.getSaleCost() : BigDecimal.ZERO)
                .add(result.getDigitalServiceCost() != null ? result.getDigitalServiceCost() : BigDecimal.ZERO)
                .add(result.getCategoryCommissionCost() != null ? result.getCategoryCommissionCost() : BigDecimal.ZERO)
                .add(result.getAdCost() != null ? result.getAdCost() : BigDecimal.ZERO)
                .add(result.getReturnCost() != null ? result.getReturnCost() : BigDecimal.ZERO)
                .add(result.getDeliveryCost() != null ? result.getDeliveryCost() : BigDecimal.ZERO)
                .add(result.getStorageCost() != null ? result.getStorageCost() : BigDecimal.ZERO);
        result.setTotalCost(totalCost.setScale(SCALE_MONEY, DEFAULT_ROUNDING));

        // 毛利润 = 售价 - 总成本
        BigDecimal grossProfit = result.getPrice().subtract(totalCost);
        result.setGrossProfit(grossProfit.setScale(SCALE_MONEY, DEFAULT_ROUNDING));

        // 毛利率 = 毛利润 / 售价 × 100%
        BigDecimal grossMargin = BigDecimal.ZERO;
        if (result.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            grossMargin = grossProfit.divide(result.getPrice(), SCALE_RATE, DEFAULT_ROUNDING)
                    .multiply(BigDecimal.valueOf(100));
        }
        result.setGrossMargin(grossMargin.setScale(SCALE_MONEY, DEFAULT_ROUNDING));

        // 净利润（暂时等于毛利润，后续可扩展）
        result.setNetProfit(grossProfit);

        // 投资回报率 = 毛利润 / 采购成本 × 100%
        BigDecimal roi = BigDecimal.ZERO;
        if (result.getPurchaseCost().compareTo(BigDecimal.ZERO) > 0) {
            roi = grossProfit.divide(result.getPurchaseCost(), SCALE_RATE, DEFAULT_ROUNDING)
                    .multiply(BigDecimal.valueOf(100));
        }
        result.setRoi(roi.setScale(SCALE_MONEY, DEFAULT_ROUNDING));
    }


    /**
     * 生成配置快照
     */
    protected abstract void generateConfigSnapshot(ProfitCalculationSaveReqVO request,
                                                   ProfitCalculationTemplateDO template,
                                                   BigDecimal exchangeRate,
                                                   ProfitCalculationResultDTO result);

    @Override
    public boolean validateRequest(ProfitCalculationSaveReqVO request, ProfitCalculationTemplateDO template) {

        // 验证必要参数
        return request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) > 0
                && request.getPurchaseUnit() != null && request.getPurchaseUnit().compareTo(BigDecimal.ZERO) > 0
                && request.getProductLength() != null && request.getProductLength().compareTo(BigDecimal.ZERO) > 0
                && request.getProductWidth() != null && request.getProductWidth().compareTo(BigDecimal.ZERO) > 0
                && request.getProductHeight() != null && request.getProductHeight().compareTo(BigDecimal.ZERO) > 0
                && request.getProductWeight() != null && request.getProductWeight().compareTo(BigDecimal.ZERO) > 0;
    }

}