package cn.iocoder.yudao.module.chrome.service.transaction;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.chrome.controller.admin.transaction.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.transaction.CreditsTransactionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 积分交易记录 Service 接口
 *
 * @author Jax
 */
public interface CreditsTransactionService {

    /**
     * 创建积分交易记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCreditsTransaction(@Valid CreditsTransactionSaveReqVO createReqVO);

    /**
     * 更新积分交易记录
     *
     * @param updateReqVO 更新信息
     */
    void updateCreditsTransaction(@Valid CreditsTransactionSaveReqVO updateReqVO);

    /**
     * 删除积分交易记录
     *
     * @param id 编号
     */
    void deleteCreditsTransaction(Long id);

    /**
     * 获得积分交易记录
     *
     * @param id 编号
     * @return 积分交易记录
     */
    CreditsTransactionDO getCreditsTransaction(Long id);

    /**
     * 获得积分交易记录分页
     *
     * @param pageReqVO 分页查询
     * @return 积分交易记录分页
     */
    PageResult<CreditsTransactionDO> getCreditsTransactionPage(CreditsTransactionPageReqVO pageReqVO);

    /**
     * 根据用户ID获得积分交易记录分页
     *
     * @param userId 用户ID
     * @param pageReqVO 分页查询
     * @return 积分交易记录分页
     */
    PageResult<CreditsTransactionDO> getCreditsTransactionPageByUserId(Long userId, CreditsTransactionPageReqVO pageReqVO);

}