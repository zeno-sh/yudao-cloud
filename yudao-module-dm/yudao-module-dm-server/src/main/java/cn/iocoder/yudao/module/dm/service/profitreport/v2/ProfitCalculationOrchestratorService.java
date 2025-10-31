package cn.iocoder.yudao.module.dm.service.profitreport.v2;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 财务利润计算业务编排器服务
 * 负责协调整个计算流程
 *
 * @author Jax
 */
public interface ProfitCalculationOrchestratorService {
    
    /**
     * 初始化并启动计算任务（参照V1版本的交互方式）
     *
     * @param request 计算请求参数
     * @return 任务初始化结果
     */
    TaskInitResultVO initializeAndStartCalculation(ProfitCalculationRequestVO request);
    
    /**
     * 获取计算结果
     *
     * @param queryReqVO 查询请求参数
     * @return 分页结果
     */
    PageResult<ProfitReportResultVO> getCalculationResult(ProfitReportQueryReqVO queryReqVO);
    
    /**
     * 导出计算结果
     *
     * @param queryReqVO 查询请求参数
     * @param response HTTP响应
     */
    void exportCalculationResult(ProfitReportQueryReqVO queryReqVO, HttpServletResponse response);
    
    /**
     * 取消计算任务
     */
    Boolean cancelCalculation(String taskId);

    /**
     * 异步执行利润计算（兼容V1版本的参数形式）
     * 
     * @param taskId 任务ID
     * @param clientIds 门店ID列表
     * @param startDate 开始日期字符串
     * @param endDate 结束日期字符串
     * @param dimension 维度
     * @param timeType 时间类型
     */
    void executeCalculationAsyncV1Compatible(String taskId, List<String> clientIds, 
                                String startDate, String endDate, 
                                String dimension, String timeType);
} 