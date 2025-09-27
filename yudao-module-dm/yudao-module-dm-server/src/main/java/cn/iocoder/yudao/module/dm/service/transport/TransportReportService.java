package cn.iocoder.yudao.module.dm.service.transport;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.TransportReportReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.TransportReportVO;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.TransportReportExportVO;

import java.util.List;

/**
 * 发货计划报表 Service 接口
 */
public interface TransportReportService {

    /**
     * 获取发货计划月度报表分页
     *
     * @param reqVO 查询条件
     * @return 发货计划月度报表分页
     */
    PageResult<TransportReportVO> getTransportReportPage(TransportReportReqVO reqVO);

    /**
     * 构建Excel导出数据
     *
     * @param monthlyList 月度数据列表
     * @return Excel导出数据列表
     */
    List<TransportReportExportVO> buildExportExcelData(List<TransportReportVO> monthlyList);
} 