package cn.iocoder.yudao.module.dm.service.profitreport;

import java.time.LocalDate;
import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 财务账单报告 Service 接口
 *
 * @author Zeno
 */
public interface ProfitReportService {

    /**
     * 创建财务账单报告
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createProfitReport(@Valid ProfitReportSaveReqVO createReqVO);

    /**
     * 批量创建财务账单报告
     *
     * @param createReqVOList
     * @return
     */
    Boolean batchCreateProfitReport(@Valid List<ProfitReportSaveReqVO> createReqVOList);

    /**
     * 更新财务账单报告
     *
     * @param updateReqVO 更新信息
     */
    void updateProfitReport(@Valid ProfitReportSaveReqVO updateReqVO);

    /**
     * 删除财务账单报告
     *
     * @param id 编号
     */
    void deleteProfitReport(Integer id);

    /**
     * 获得财务账单报告
     *
     * @param id 编号
     * @return 财务账单报告
     */
    ProfitReportDO getProfitReport(Integer id);

    /**
     * 获得财务账单报告分页
     *
     * @param pageReqVO 分页查询
     * @return 财务账单报告分页
     */
    PageResult<ProfitReportDO> getProfitReportPage(ProfitReportPageReqVO pageReqVO);

    /**
     * 获取门店维度分页
     *
     * @param page
     * @param pageReqVO
     * @return
     */
    IPage<ProfitReportRespVO> getClientProfitReportPage(IPage<ProfitReportRespVO> page, ProfitReportPageReqVO pageReqVO);

    /**
     * 获取产品维度分页
     *
     * @param page
     * @param pageReqVO
     * @return
     */
    IPage<ProfitReportRespVO> getSkuProfitReportPage(IPage<ProfitReportRespVO> page, ProfitReportPageReqVO pageReqVO);

    /**
     * 删除财务账单报告
     *
     * @param clientIds
     * @param finishDate
     */
    void deleteProfitReport(String[] clientIds, LocalDate[] finishDate);
}