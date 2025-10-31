package cn.iocoder.yudao.module.dm.service.transaction;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportPageReqVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;

import cn.iocoder.yudao.module.dm.controller.admin.transaction.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.transaction.OzonFinanceTransactionMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 交易记录 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class OzonFinanceTransactionServiceImpl implements OzonFinanceTransactionService {

    @Resource
    private OzonFinanceTransactionMapper ozonFinanceTransactionMapper;

    @Override
    public Long createOzonFinanceTransaction(OzonFinanceTransactionSaveReqVO createReqVO) {
        // 插入
        OzonFinanceTransactionDO ozonFinanceTransaction = BeanUtils.toBean(createReqVO, OzonFinanceTransactionDO.class);
        ozonFinanceTransactionMapper.insert(ozonFinanceTransaction);
        // 返回
        return ozonFinanceTransaction.getId();
    }

    @Override
    public void updateOzonFinanceTransaction(OzonFinanceTransactionSaveReqVO updateReqVO) {
        // 校验存在
        validateOzonFinanceTransactionExists(updateReqVO.getId());
        // 更新
        OzonFinanceTransactionDO updateObj = BeanUtils.toBean(updateReqVO, OzonFinanceTransactionDO.class);
        ozonFinanceTransactionMapper.updateById(updateObj);
    }

    @Override
    public void batchUpdateTransactions(List<OzonFinanceTransactionDO> updateList) {
        ozonFinanceTransactionMapper.updateBatch(updateList);
    }

    @Override
    public void batchSaveTransactions(List<OzonFinanceTransactionDO> saveList) {
        ozonFinanceTransactionMapper.insertBatch(saveList);
    }

    @Override
    public void deleteOzonFinanceTransaction(Long id) {
        // 校验存在
        validateOzonFinanceTransactionExists(id);
        // 删除
        ozonFinanceTransactionMapper.deleteById(id);
    }

    private void validateOzonFinanceTransactionExists(Long id) {
        if (ozonFinanceTransactionMapper.selectById(id) == null) {
            throw exception(OZON_FINANCE_TRANSACTION_NOT_EXISTS);
        }
    }

    @Override
    public OzonFinanceTransactionDO getOzonFinanceTransaction(Long id) {
        return ozonFinanceTransactionMapper.selectById(id);
    }

    @Override
    public List<OzonFinanceTransactionDO> batchOzonFinanceTransactionList(Collection<Long> operationIds) {
        return ozonFinanceTransactionMapper.selectList(OzonFinanceTransactionDO::getOperationId, operationIds);
    }

    @Override
    public OzonFinanceTransactionDO getTransactionByOperationId(Long operationId) {
        return ozonFinanceTransactionMapper.selectOne(OzonFinanceTransactionDO::getOperationId, operationId);
    }

    @Override
    public PageResult<OzonFinanceTransactionDO> getOzonFinanceTransactionPage(OzonFinanceTransactionPageReqVO pageReqVO) {
        return ozonFinanceTransactionMapper.selectPage(pageReqVO);
    }

    @Override
    public IPage<OzonServiceTransactionRespVO> selectServicePage(IPage<OzonServiceTransactionRespVO> iPage, OzonFinanceTransactionPageReqVO pageReqVO) {
        return ozonFinanceTransactionMapper.selectServicePage(iPage, pageReqVO);
    }

    @Override
    public List<OzonFinanceTransactionDO> getAllServices(OzonFinanceTransactionPageReqVO pageReqVO) {
        LambdaQueryWrapperX<OzonFinanceTransactionDO> queryWrapperX = new LambdaQueryWrapperX<OzonFinanceTransactionDO>()
                .inIfPresent(OzonFinanceTransactionDO::getClientId, pageReqVO.getClientIds())
                .betweenIfPresent(OzonFinanceTransactionDO::getOperationDate, pageReqVO.getOperationDate()[0], pageReqVO.getOperationDate()[1])
                .eqIfPresent(OzonFinanceTransactionDO::getType, "services");
        return ozonFinanceTransactionMapper.selectList(queryWrapperX);
    }

    @Override
    public List<OzonFinanceTransactionDO> batchTransactionListByOrderNumbers(Collection<String> orderNumbers) {
        LambdaQueryWrapperX<OzonFinanceTransactionDO> queryWrapperX = new LambdaQueryWrapperX<OzonFinanceTransactionDO>()
                .inIfPresent(OzonFinanceTransactionDO::getPostingNumber, orderNumbers);
        return ozonFinanceTransactionMapper.selectList(queryWrapperX);
    }

    @Override
    public List<OzonFinanceTransactionDO> getSigendTransactionList(Collection<String> clientIds, LocalDate[] settledDate) {
        // 重要修改：返回所有类型的财务交易记录，让后续的计算器根据自己的需要过滤
        // - "orders" 类型用于签收订单计算  
        // - "returns" 类型用于退货计算
        // - "other" 类型用于收单费用计算
        // 之前错误地只返回"orders"类型，导致其他计算器无法获取到需要的数据
        LambdaQueryWrapperX<OzonFinanceTransactionDO> queryWrapperX = new LambdaQueryWrapperX<OzonFinanceTransactionDO>()
                .inIfPresent(OzonFinanceTransactionDO::getClientId, clientIds)
                .betweenIfPresent(OzonFinanceTransactionDO::getOperationDate, settledDate[0], settledDate[1]);
        return ozonFinanceTransactionMapper.selectList(queryWrapperX);
    }

    @Override
    public List<OzonFinanceTransactionDO> batchTransactionListByPostingNumbers(Collection<String> postingNumbers) {
        LambdaQueryWrapperX<OzonFinanceTransactionDO> queryWrapperX = new LambdaQueryWrapperX<OzonFinanceTransactionDO>()
                .inIfPresent(OzonFinanceTransactionDO::getPostingNumber, postingNumbers);
        return ozonFinanceTransactionMapper.selectList(queryWrapperX);
    }

    @Override
    public List<OzonFinanceTransactionDO> getCompensationTransactions(ProfitReportPageReqVO pageReqVO) {
        LambdaQueryWrapperX<OzonFinanceTransactionDO> queryWrapperX = new LambdaQueryWrapperX<OzonFinanceTransactionDO>()
                .inIfPresent(OzonFinanceTransactionDO::getClientId, pageReqVO.getClientIds())
                .betweenIfPresent(OzonFinanceTransactionDO::getOperationDate, pageReqVO.getFinanceDate()[0], pageReqVO.getFinanceDate()[1])
                .eqIfPresent(OzonFinanceTransactionDO::getType, "compensation");
        return ozonFinanceTransactionMapper.selectList(queryWrapperX);
    }
}