package cn.iocoder.yudao.module.dm.service.transaction;

import java.time.LocalDate;
import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.transaction.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 交易记录 Service 接口
 *
 * @author Zeno
 */
public interface OzonFinanceTransactionService {

    /**
     * 创建交易记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOzonFinanceTransaction(@Valid OzonFinanceTransactionSaveReqVO createReqVO);

    /**
     * 更新交易记录
     *
     * @param updateReqVO 更新信息
     */
    void updateOzonFinanceTransaction(@Valid OzonFinanceTransactionSaveReqVO updateReqVO);

    /**
     * 批量更新交易记录
     * @param updateList
     */
    void batchUpdateTransactions(List<OzonFinanceTransactionDO> updateList);

    /**
     * 批量保存交易记录
     * @param saveList
     */
    void batchSaveTransactions(List<OzonFinanceTransactionDO> saveList);

    /**
     * 删除交易记录
     *
     * @param id 编号
     */
    void deleteOzonFinanceTransaction(Long id);

    /**
     * 获得交易记录
     *
     * @param id 编号
     * @return 交易记录
     */
    OzonFinanceTransactionDO getOzonFinanceTransaction(Long id);

    /**
     * 批量查询交易记录
     *
     * @param operationIds
     * @return
     */
    List<OzonFinanceTransactionDO> batchOzonFinanceTransactionList(Collection<Long> operationIds);

    /**
     * 获得交易记录分页
     *
     * @param pageReqVO 分页查询
     * @return 交易记录分页
     */
    PageResult<OzonFinanceTransactionDO> getOzonFinanceTransactionPage(OzonFinanceTransactionPageReqVO pageReqVO);

    /**
     * 广告分页
     *
     * @param iPage
     * @param pageReqVO
     * @return
     */
    IPage<OzonServiceTransactionRespVO> selectServicePage(IPage<OzonServiceTransactionRespVO> iPage, OzonFinanceTransactionPageReqVO pageReqVO);

    /**
     * 获取所有服务费，不包含广告（模板+冲上顶端）
     * @param pageReqVO
     * @return
     */
    List<OzonFinanceTransactionDO> getAllServices(OzonFinanceTransactionPageReqVO pageReqVO);

    /**
     * 获得交易记录
     *
     * @param operationId 查询条件
     * @return 交易记录列表
     */
    OzonFinanceTransactionDO getTransactionByOperationId(Long operationId);

    /**
     * 批量查询交易记录
     *
     * @param orderNumbers
     * @return
     */
    List<OzonFinanceTransactionDO> batchTransactionListByOrderNumbers(Collection<String> orderNumbers);

    /**
     * 获取已签收订单
     *
     * @param clientId
     * @param settledDate
     * @return
     */
    List<OzonFinanceTransactionDO> getSigendTransactionList(Collection<String> clientId, LocalDate[] settledDate);

    /**
     * 批量查询交易记录
     *
     * @param postingNumbers
     * @return
     */
    List<OzonFinanceTransactionDO> batchTransactionListByPostingNumbers(Collection<String> postingNumbers);

    /**
     * 获取补偿金额交易记录
     *
     * @param pageReqVO 查询条件
     * @return 补偿金额交易记录列表
     */
    List<OzonFinanceTransactionDO> getCompensationTransactions(ProfitReportPageReqVO pageReqVO);
}