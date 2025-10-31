package cn.iocoder.yudao.module.dm.dal.mysql.profitreport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportTaskLogDO;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportTaskLogPageReqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 财务账单报告计算任务日志 Mapper
 *
 * @author zeno
 */
@Mapper
public interface ProfitReportTaskLogMapper extends BaseMapperX<ProfitReportTaskLogDO> {

    /**
     * 查询任务日志分页
     *
     * @param reqVO 请求参数
     * @return 任务日志分页
     */
    default PageResult<ProfitReportTaskLogDO> selectPage(ProfitReportTaskLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProfitReportTaskLogDO>()
                .eqIfPresent(ProfitReportTaskLogDO::getTaskId, reqVO.getTaskId())
                .eqIfPresent(ProfitReportTaskLogDO::getDimension, reqVO.getDimension())
                .eqIfPresent(ProfitReportTaskLogDO::getTimeType, reqVO.getTimeType())
                .eqIfPresent(ProfitReportTaskLogDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProfitReportTaskLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProfitReportTaskLogDO::getId));
    }

    /**
     * 查询最近的任务日志
     *
     * @param limit 限制条数
     * @return 任务日志列表
     */
    default List<ProfitReportTaskLogDO> selectLatestTasks(int limit) {
        return selectList(new LambdaQueryWrapperX<ProfitReportTaskLogDO>()
                .orderByDesc(ProfitReportTaskLogDO::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 根据任务ID查询任务日志
     *
     * @param taskId 任务ID
     * @return 任务日志
     */
    default ProfitReportTaskLogDO selectByTaskId(String taskId) {
        return selectOne(ProfitReportTaskLogDO::getTaskId, taskId);
    }
} 