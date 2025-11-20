package cn.iocoder.yudao.module.dm.service.calculation.strategy;


import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.template.ProfitCalculationTemplateDO;
import cn.iocoder.yudao.module.dm.service.calculation.dto.ProfitCalculationResultDTO;

import java.math.BigDecimal;

/**
 * 国家特定计算策略接口
 * 
 * 遵循策略模式：每个国家的计算逻辑作为独立的策略实现
 * 遵循开闭原则：新增国家时只需新增策略实现，无需修改现有代码
 * 遵循单一职责原则：每个策略只负责特定国家的计算逻辑
 *
 * @author Jax
 */
public interface CountryCalculationStrategy {

    /**
     * 获取支持的国家代码
     * 
     * @return 国家代码（如：US, DE, UK, CA, JP）
     */
    String getSupportedCountry();

    /**
     * 执行国家特定的利润计算
     * 
     * @param request 计算请求
     * @param template 配置模板
     * @param exchangeRate 汇率
     * @return 计算结果
     */
    ProfitCalculationResultDTO calculate(ProfitCalculationSaveReqVO request,
                                         ProfitCalculationTemplateDO template,
                                         BigDecimal exchangeRate);

    /**
     * 验证请求参数是否符合该国家的特殊要求
     * 
     * @param request 计算请求
     * @param template 配置模板
     * @return 验证结果
     */
    boolean validateRequest(ProfitCalculationSaveReqVO request, ProfitCalculationTemplateDO template);

    /**
     * 获取该国家的特殊计算说明
     * 
     * @return 计算说明
     */
    String getCalculationDescription();

}