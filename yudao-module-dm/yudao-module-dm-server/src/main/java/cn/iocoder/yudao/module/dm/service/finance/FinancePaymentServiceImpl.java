package cn.iocoder.yudao.module.dm.service.finance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.dm.controller.admin.finance.vo.FinancePaymentPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.finance.vo.FinancePaymentSaveFileReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.finance.vo.FinancePaymentSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.finance.FinancePaymentItemDO;
import cn.iocoder.yudao.module.dm.dal.mysql.finance.DmFinancePaymentItemMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.finance.DmFinancePaymentMapper;
import cn.iocoder.yudao.module.dm.dal.redis.dao.DmNoRedisDAO;
import cn.iocoder.yudao.module.dm.enums.DmBizTypeEnum;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseOrderService;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 付款单 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class FinancePaymentServiceImpl implements FinancePaymentService {

    public static final String PROCESS_KEY = "dm_finance_payment_flow";

    @Resource
    private DmFinancePaymentMapper dmFinancePaymentMapper;
    @Resource
    private DmFinancePaymentItemMapper dmFinancePaymentItemMapper;
    @Resource
    private DmNoRedisDAO dmNoRedisDAO;
    @Resource
    private PurchaseOrderService purchaseOrderService;
    @Resource
    private BpmProcessInstanceApi bpmProcessInstanceApi;
    @Resource
    private ConfigApi configApi;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFinancePayment(FinancePaymentSaveReqVO createReqVO) {

        // 生成付款单号，并校验唯一性
        String no = dmNoRedisDAO.generate(dmNoRedisDAO.FINANCE_PAYMENT_NO_PREFIX);
        if (dmFinancePaymentMapper.selectByNo(no) != null) {
            throw exception(FINANCE_PAYMENT_NO_EXISTS);
        }

        // 插入
        FinancePaymentDO financePayment = BeanUtils.toBean(createReqVO, FinancePaymentDO.class,
                payment -> payment.setNo(no));

        calculateTotalPrice(financePayment, createReqVO.getFinancePaymentItems());
        dmFinancePaymentMapper.insert(financePayment);

        // 插入子表
        createFinancePaymentItemList(financePayment.getId(), createReqVO.getFinancePaymentItems());

        // 只有审批通过才能改变金额，先注释掉
//        updatePurchasePrice(createReqVO.getFinancePaymentItems());
        // 返回
        return financePayment.getId();
    }

    private void calculateTotalPrice(FinancePaymentDO payment, List<FinancePaymentItemDO> paymentItems) {
        payment.setTotalPrice(getSumValue(paymentItems, FinancePaymentItemDO::getPaymentPrice, BigDecimal::add, BigDecimal.ZERO));
        payment.setPaymentPrice(payment.getTotalPrice().subtract(payment.getDiscountPrice()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFinancePayment(FinancePaymentSaveReqVO updateReqVO) {
        // 校验存在
        validateFinancePaymentExists(updateReqVO.getId());
        // 更新
        FinancePaymentDO updateObj = BeanUtils.toBean(updateReqVO, FinancePaymentDO.class);
        // 重置审核状态
        updateObj.setAuditStatus(BpmTaskStatusEnum.WAIT.getStatus());
        dmFinancePaymentMapper.updateById(updateObj);

        // 更新子表
        updateFinancePaymentItemList(updateReqVO.getId(), updateReqVO.getFinancePaymentItems());
    }

    @Override
    public void updateFinanceFiles(FinancePaymentSaveFileReqVO financePaymentSaveFileReqVO) {
        // 校验存在
        validateFinancePaymentExists(financePaymentSaveFileReqVO.getId());
        FinancePaymentDO updateObj = BeanUtils.toBean(financePaymentSaveFileReqVO, FinancePaymentDO.class);
        dmFinancePaymentMapper.updateById(updateObj);
    }

    private void validateFinancePaymentApprove(Long id) {
        FinancePaymentDO financePaymentDO = dmFinancePaymentMapper.selectById(id);
        if (BpmTaskStatusEnum.APPROVE.getStatus().equals(financePaymentDO.getAuditStatus())) {
            throw exception(FINANCE_PAYMENT_UPDATE_FAIL_APPROVE, financePaymentDO.getNo());
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinancePayment(Long id) {
        // 校验存在
        validateFinancePaymentExists(id);
        // 校验不处于审批
        validateFinancePaymentApprove(id);

        // 2.1 删除付款单
        dmFinancePaymentMapper.deleteById(id);
        // 2.2 删除付款单项
        List<FinancePaymentItemDO> paymentItems = dmFinancePaymentItemMapper.selectListByPaymentId(id);
        dmFinancePaymentItemMapper.deleteBatchIds(convertSet(paymentItems, FinancePaymentItemDO::getId));

        // 2.3 更新付款金额情况
        updatePurchasePrice(paymentItems);
    }

    private void validateFinancePaymentExists(Long id) {
        if (dmFinancePaymentMapper.selectById(id) == null) {
            throw exception(FINANCE_PAYMENT_NOT_EXISTS);
        }
    }

    @Override
    public FinancePaymentDO getFinancePayment(Long id) {
        return dmFinancePaymentMapper.selectById(id);
    }

    @Override
    public void submitReview(Long paymentId) {

        String value = configApi.getConfigValueByKey("dm_finance_flow_switch").getCheckedData();

        // 如果审批流启用，则使用BPM流程，否则直接默认审批通过
        if (value.equals("true")) {
            // 发起 BPM 流程
            Map<String, Object> processInstanceExtras = new HashMap<>();
            processInstanceExtras.put("paymentId", paymentId);

            Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
            String processInstanceId = bpmProcessInstanceApi.createProcessInstance(loginUserId, new BpmProcessInstanceCreateReqDTO()
                    .setVariables(processInstanceExtras)
                    .setProcessDefinitionKey(PROCESS_KEY).setBusinessKey(String.valueOf(paymentId))).getCheckedData();

            dmFinancePaymentMapper.updateById(new FinancePaymentDO().setId(paymentId).setProcessInstanceId(processInstanceId)
                    .setAuditStatus(BpmTaskStatusEnum.RUNNING.getStatus()));
        } else {
            dmFinancePaymentMapper.updateById(new FinancePaymentDO().setId(paymentId)
                    .setAuditStatus(BpmTaskStatusEnum.APPROVE.getStatus()));

            updateAuditStatus(paymentId, BpmTaskStatusEnum.APPROVE.getStatus());
        }
    }

    @Override
    public void updateAuditStatus(Long id, Integer auditStatus) {
        validateFinancePaymentExists(id);
        dmFinancePaymentMapper.updateById(new FinancePaymentDO().setId(id).setAuditStatus(auditStatus));

        // 查询所有item，每个item关联了采购单
        List<FinancePaymentItemDO> financePaymentItemList = getFinancePaymentItemListByPaymentId(id);

        // 再次反查所有bizId的付款记录
        List<FinancePaymentItemDO> historyFinancePaymentItemList = getFinancePaymentItemByBizIds(financePaymentItemList.stream().map(FinancePaymentItemDO::getBizId).distinct().collect(Collectors.toList()));
        Map<Long, BigDecimal> poPaidPriceMap = historyFinancePaymentItemList.stream()
                .collect(Collectors.groupingBy(FinancePaymentItemDO::getBizId,
                        Collectors.mapping(FinancePaymentItemDO::getPaymentPrice,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));


        // 只有审批通过，才能算真正付款
        if (BpmTaskStatusEnum.APPROVE.getStatus().equals(auditStatus)) {
            poPaidPriceMap.forEach((poId, paidPrice) -> {
                // 更新付款金额
                purchaseOrderService.updatePaymentPrice(poId, paidPrice);
            });
        }
    }

    private List<FinancePaymentItemDO> getFinancePaymentItemByBizIds(Collection<Long> bizIds) {
        return dmFinancePaymentItemMapper.selectList(FinancePaymentItemDO::getBizId, bizIds);
    }

    @Override
    public PageResult<FinancePaymentDO> getFinancePaymentPage(FinancePaymentPageReqVO pageReqVO) {
        return dmFinancePaymentMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（ERP 付款项） ====================

    @Override
    public List<FinancePaymentItemDO> getFinancePaymentItemListByPaymentId(Long paymentId) {
        return dmFinancePaymentItemMapper.selectListByPaymentId(paymentId);
    }

    private void createFinancePaymentItemList(Long paymentId, List<FinancePaymentItemDO> list) {
        list.forEach(o -> o.setPaymentId(paymentId));
        dmFinancePaymentItemMapper.insertBatch(list);
    }

    private void updateFinancePaymentItemList(Long paymentId, List<FinancePaymentItemDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<FinancePaymentItemDO> oldList = dmFinancePaymentItemMapper.selectListByPaymentId(paymentId);
        List<List<FinancePaymentItemDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setPaymentId(paymentId));
            dmFinancePaymentItemMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            dmFinancePaymentItemMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            dmFinancePaymentItemMapper.deleteBatchIds(convertList(diffList.get(2), FinancePaymentItemDO::getId));
        }

        // 第三步，更新采购单付款金额情况
        updatePurchasePrice(CollectionUtils.newArrayList(diffList));
    }

    private void updatePurchasePrice(List<FinancePaymentItemDO> paymentItems) {
        paymentItems.forEach(paymentItem -> {
            BigDecimal totalPaymentPrice = dmFinancePaymentItemMapper.selectPaymentPriceSumByBizIdAndBizType(
                    paymentItem.getBizId(), paymentItem.getBizType());
            if (DmBizTypeEnum.PURCHASE_ORDER.getType().equals(paymentItem.getBizType())) {
                purchaseOrderService.updatePaymentPrice(paymentItem.getBizId(), totalPaymentPrice);
            } else {
                throw new IllegalArgumentException("业务类型不正确：" + paymentItem.getBizType());
            }
        });
    }

    private void deleteFinancePaymentItemByPaymentId(Long paymentId) {
        dmFinancePaymentItemMapper.deleteByPaymentId(paymentId);
    }

}