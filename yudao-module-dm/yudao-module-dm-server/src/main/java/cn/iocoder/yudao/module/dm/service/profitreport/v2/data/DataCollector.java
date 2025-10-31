package cn.iocoder.yudao.module.dm.service.profitreport.v2.data;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitCalculationRequestVO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.DataCollectionResult;

/**
 * 数据收集器接口
 * 负责收集计算所需的所有基础数据
 *
 * @author Jax
 */
public interface DataCollector {
    
    /**
     * 收集所有计算所需数据
     *
     * @param request 计算请求参数
     * @param taskId 任务ID
     * @return 数据收集结果
     */
    DataCollectionResult collectData(ProfitCalculationRequestVO request, String taskId);
} 