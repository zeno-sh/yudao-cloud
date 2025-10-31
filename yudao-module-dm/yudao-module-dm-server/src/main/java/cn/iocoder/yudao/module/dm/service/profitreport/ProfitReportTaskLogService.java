package cn.iocoder.yudao.module.dm.service.profitreport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportTaskLogPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportTaskLogDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.ProfitReportTaskStatusEnum;

import java.time.LocalDate;
import java.util.List;

/**
 * 财务账单报告计算任务日志 Service 接口
 *
 * @author zeno
 */
public interface ProfitReportTaskLogService {
    
    /**
     * 创建任务日志
     *
     * @param clientIds 客户端ID数组
     * @param dimension 查询维度
     * @param timeType 时间类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 任务ID
     */
    String createTaskLog(String[] clientIds, String dimension, String timeType, 
                         LocalDate startDate, LocalDate endDate);
    
    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 状态
     * @param errorInfo 错误信息
     * @param executeLog 执行日志
     * @param affectedRecords 影响的记录数
     */
    void updateTaskStatus(String taskId, ProfitReportTaskStatusEnum status, 
                        String errorInfo, String executeLog, Integer affectedRecords);
    
    /**
     * 追加执行日志
     *
     * @param taskId 任务ID
     * @param logInfo 日志信息
     */
    void appendExecuteLog(String taskId, String logInfo);
    
    /**
     * 追加错误信息
     *
     * @param taskId 任务ID
     * @param errorInfo 错误信息
     */
    void appendErrorInfo(String taskId, String errorInfo);
    
    /**
     * 获取任务日志
     *
     * @param taskId 任务ID
     * @return 任务日志
     */
    ProfitReportTaskLogDO getTaskLog(String taskId);
    
    /**
     * 获取任务日志分页
     *
     * @param pageReqVO 分页请求
     * @return 任务日志分页
     */
    PageResult<ProfitReportTaskLogDO> getTaskLogPage(ProfitReportTaskLogPageReqVO pageReqVO);
    
    /**
     * 获取最近的任务日志
     *
     * @param limit 限制条数
     * @return 任务日志列表
     */
    List<ProfitReportTaskLogDO> getLatestTaskLogs(int limit);
} 