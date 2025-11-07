package cn.iocoder.yudao.module.dm.service.report;

import cn.iocoder.yudao.module.dm.controller.admin.report.vo.SkuReportQueryReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.report.vo.SkuReportRespVO;

import java.util.List;

/**
 * SKU报表 Service 接口
 *
 * @author Jax
 */
public interface SkuReportService {

    /**
     * 查询SKU报表数据
     *
     * @param reqVO 查询请求参数
     * @return SKU报表数据列表
     */
    List<SkuReportRespVO> querySkuReport(SkuReportQueryReqVO reqVO);
}
