package cn.iocoder.yudao.module.dm.service.profitreport.v2.storage;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitReportQueryReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2.ProfitReportResultVO;
import cn.iocoder.yudao.module.dm.service.profitreport.v2.model.AggregationResult;

import java.util.List;

/**
 * 财务账单报告结果存储服务接口
 *
 * @author Jax
 */
public interface ProfitReportV2StorageService {

    /**
     * 保存聚合结果到数据库
     *
     * @param aggregationResult 聚合结果
     * @param taskId 任务ID
     * @return 保存的记录数
     */
    int saveAggregationResult(AggregationResult aggregationResult, String taskId);

    /**
     * 分页查询利润报告结果
     *
     * @param queryReqVO 查询条件
     * @return 分页结果
     */
    PageResult<ProfitReportResultVO> getResultsByPage(ProfitReportQueryReqVO queryReqVO);

    /**
     * 查询利润报告结果列表（用于导出）
     *
     * @param queryReqVO 查询条件
     * @return 结果列表
     */
    List<ProfitReportResultVO> getResultsList(ProfitReportQueryReqVO queryReqVO);

    /**
     * 根据任务ID删除结果
     *
     * @param taskId 任务ID
     * @return 删除的记录数
     */
    int deleteResultsByTaskId(String taskId);

    /**
     * 根据任务ID查询结果数量
     *
     * @param taskId 任务ID
     * @return 记录数量
     */
    Long countResultsByTaskId(String taskId);

    /**
     * 清理过期数据
     *
     * @param retentionDays 保留天数
     * @return 清理的记录数
     */
    int cleanupExpiredData(int retentionDays);

    /**
     * 检查任务结果是否存在
     *
     * @param taskId 任务ID
     * @return 是否存在
     */
    boolean existsResultsByTaskId(String taskId);

    /**
     * 根据clientId和日期范围删除结果
     * @param clientId 客户端ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 删除的记录数
     */
    int deleteResultsByClientIdAndDate(String clientId, java.time.LocalDate startDate, java.time.LocalDate endDate);
} 