package cn.iocoder.yudao.module.dm.service.calculation;



import cn.iocoder.yudao.module.dm.controller.admin.calculation.vo.ProfitCalculationSaveReqVO;
import cn.iocoder.yudao.module.dm.service.calculation.dto.ProfitCalculationResultDTO;

import javax.validation.Valid;
import java.util.List;

/**
 * 利润测算算法服务接口
 * 
 * 遵循单一职责原则：专门负责利润测算的核心算法逻辑
 * 遵循接口隔离原则：提供精简的算法接口，不包含数据持久化等其他职责
 *
 * @author Jax
 */
public interface ProfitCalculationAlgorithmService {

    /**
     * 单个产品利润测算
     * 
     * @param saveReqVO 保存请求参数
     * @return 测算结果
     */
    ProfitCalculationResultDTO calculateProfit(@Valid ProfitCalculationSaveReqVO saveReqVO);

    /**
     * 批量产品利润测算
     * 
     * @param saveReqVOs 批量保存请求参数
     * @return 批量测算结果
     */
    List<ProfitCalculationResultDTO> batchCalculateProfit(@Valid List<ProfitCalculationSaveReqVO> saveReqVOs);

    /**
     * 获取支持的国家列表
     * 
     * @return 支持的国家代码集合
     */
    java.util.Set<String> getSupportedCountries();

    /**
     * 获取指定国家的计算说明
     * 
     * @param countryCode 国家代码
     * @return 计算说明
     */
    String getCalculationDescription(String countryCode);

    /**
     * 获取所有支持国家的计算说明
     * 
     * @return 国家代码到计算说明的映射
     */
    java.util.Map<String, String> getAllCalculationDescriptions();

}