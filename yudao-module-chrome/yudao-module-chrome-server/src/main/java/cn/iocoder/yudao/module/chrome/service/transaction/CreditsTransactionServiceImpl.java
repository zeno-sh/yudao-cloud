package cn.iocoder.yudao.module.chrome.service.transaction;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.chrome.controller.admin.transaction.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.transaction.CreditsTransactionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.chrome.dal.mysql.transaction.CreditsTransactionMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * 积分交易记录 Service 实现类
 *
 * @author Jax
 */
@Service
@Validated
public class CreditsTransactionServiceImpl implements CreditsTransactionService {

    @Resource
    private CreditsTransactionMapper creditsTransactionMapper;

    @Override
    public Long createCreditsTransaction(CreditsTransactionSaveReqVO createReqVO) {
        // 插入
        CreditsTransactionDO creditsTransaction = BeanUtils.toBean(createReqVO, CreditsTransactionDO.class);
        creditsTransactionMapper.insert(creditsTransaction);
        // 返回
        return creditsTransaction.getId();
    }

    @Override
    public void updateCreditsTransaction(CreditsTransactionSaveReqVO updateReqVO) {
        // 校验存在
        validateCreditsTransactionExists(updateReqVO.getId());
        // 更新
        CreditsTransactionDO updateObj = BeanUtils.toBean(updateReqVO, CreditsTransactionDO.class);
        creditsTransactionMapper.updateById(updateObj);
    }

    @Override
    public void deleteCreditsTransaction(Long id) {
        // 校验存在
        validateCreditsTransactionExists(id);
        // 删除
        creditsTransactionMapper.deleteById(id);
    }

    private void validateCreditsTransactionExists(Long id) {
        if (creditsTransactionMapper.selectById(id) == null) {
            throw exception(CREDITS_TRANSACTION_NOT_EXISTS);
        }
    }

    @Override
    public CreditsTransactionDO getCreditsTransaction(Long id) {
        return creditsTransactionMapper.selectById(id);
    }

    @Override
    public PageResult<CreditsTransactionDO> getCreditsTransactionPage(CreditsTransactionPageReqVO pageReqVO) {
        return creditsTransactionMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<CreditsTransactionDO> getCreditsTransactionPageByUserId(Long userId, CreditsTransactionPageReqVO pageReqVO) {
        // 设置用户ID到查询条件中
        pageReqVO.setUserId(userId);
        return creditsTransactionMapper.selectPage(pageReqVO);
    }

}