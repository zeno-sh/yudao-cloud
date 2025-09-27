package cn.iocoder.yudao.module.dm.service.finance;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.dm.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 付款单 Service 接口
 *
 * @author Zeno
 */
public interface FinancePaymentService {

    /**
     * 创建付款单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFinancePayment(@Valid FinancePaymentSaveReqVO createReqVO);

    /**
     * 更新付款单
     *
     * @param updateReqVO 更新信息
     */
    void updateFinancePayment(@Valid FinancePaymentSaveReqVO updateReqVO);

    /**
     * 更新付款凭证
     *
     * @param financePaymentSaveFileReqVO
     */
    void updateFinanceFiles(@Valid FinancePaymentSaveFileReqVO financePaymentSaveFileReqVO);

    /**
     * 删除付款单
     *
     * @param id 编号
     */
    void deleteFinancePayment(Long id);

    /**
     * 获得付款单
     *
     * @param id 编号
     * @return 付款单
     */
    FinancePaymentDO getFinancePayment(Long id);

    /**
     * 提交审批付款单
     *
     * @param id
     */
    void submitReview(Long id);

    void updateAuditStatus(Long id, Integer auditStatus);

    /**
     * 获得付款单分页
     *
     * @param pageReqVO 分页查询
     * @return 付款单分页
     */
    PageResult<FinancePaymentDO> getFinancePaymentPage(FinancePaymentPageReqVO pageReqVO);

    // ==================== 子表（ERP 付款项） ====================

    /**
     * 获得ERP 付款项列表
     *
     * @param paymentId 付款单编号
     * @return ERP 付款项列表
     */
    List<FinancePaymentItemDO> getFinancePaymentItemListByPaymentId(Long paymentId);

}