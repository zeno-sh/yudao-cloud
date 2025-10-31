package cn.iocoder.yudao.module.dm.service.purchase.log;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogListSaveReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogPageReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.purchase.log.vo.PurchaseOrderArrivedLogSaveReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorder.PurchaseOrderItemDO;
import cn.iocoder.yudao.module.dm.enums.PurchaseOrderStatusEnum;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog.PurchaseOrderArrivedLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.purchaseorderlog.PurchaseOrderArrivedLogMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 采购单到货日志 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class PurchaseOrderArrivedLogServiceImpl implements PurchaseOrderArrivedLogService {

    @Resource
    private PurchaseOrderArrivedLogMapper purchaseOrderArrivedLogMapper;
    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Override
    public Long createPurchaseOrderArrivedLog(PurchaseOrderArrivedLogSaveReqVO createReqVO) {
        // 插入
        PurchaseOrderArrivedLogDO purchaseOrderArrivedLog = BeanUtils.toBean(createReqVO, PurchaseOrderArrivedLogDO.class);
        purchaseOrderArrivedLogMapper.insert(purchaseOrderArrivedLog);
        // 返回
        return purchaseOrderArrivedLog.getId();
    }

    @Override
    public Boolean batchCreatePurchaseOrderArrivedLog(PurchaseOrderArrivedLogListSaveReqVO arrivedLogListSaveReqVO) {
        List<PurchaseOrderArrivedLogSaveReqVO> arrivedLogList = arrivedLogListSaveReqVO.getArrivedLogList();
        //校验到货数量
        validateArrivedQuantity(arrivedLogListSaveReqVO);

        List<PurchaseOrderArrivedLogDO> arrivedLogDOList = BeanUtils.toBean(arrivedLogList, PurchaseOrderArrivedLogDO.class);

        // 过滤0的数据
        arrivedLogDOList = arrivedLogDOList.stream().filter(o -> o.getArrivedQuantity() > 0).collect(Collectors.toList());
        Boolean result = purchaseOrderArrivedLogMapper.insertBatch(arrivedLogDOList);

        // 1.计算历史到货数量+当前到货数量
        // 2.更新采购单主表到货数量
        // 3.判断到货数量与采购数量是否一致，修改采购单主状态
        updateArrivedQuantity(arrivedLogListSaveReqVO);
        return result;
    }

    private void updateArrivedQuantity(PurchaseOrderArrivedLogListSaveReqVO arrivedLogListSaveReqVO) {
        List<PurchaseOrderArrivedLogSaveReqVO> currentArrivedLogList = arrivedLogListSaveReqVO.getArrivedLogList();
        Long purchaseOrderId = currentArrivedLogList.get(0).getPurchaseOrderId();


        List<PurchaseOrderArrivedLogDO> histroyArrivedLogList = purchaseOrderService.getPurchaseOrderArrivedLogListByPurchaseOrderId(purchaseOrderId);
        int totalarrivedquantity = histroyArrivedLogList.stream().mapToInt(PurchaseOrderArrivedLogDO::getArrivedQuantity).sum();

        purchaseOrderService.updatePurchaseOrderStatus(purchaseOrderId, PurchaseOrderStatusEnum.SUCCESS.getStatus());
        purchaseOrderService.updateArriveQuantity(purchaseOrderId, totalarrivedquantity);
    }

    private void validateArrivedQuantity(PurchaseOrderArrivedLogListSaveReqVO arrivedLogListSaveReqVO) {
        List<PurchaseOrderArrivedLogSaveReqVO> arrivedLogList = arrivedLogListSaveReqVO.getArrivedLogList();
        Long purchaseOrderId = arrivedLogList.get(0).getPurchaseOrderId();

        PurchaseOrderDO purchaseOrder = purchaseOrderService.getPurchaseOrder(purchaseOrderId);
        Integer totalArrivedQuantity = purchaseOrder.getArriveQuantity();
        Integer totalCount = purchaseOrder.getTotalCount();

        // 当前到货数量不得大于待到货数量
        int totalCurrentArrivedQuantity = arrivedLogList.stream().mapToInt(PurchaseOrderArrivedLogSaveReqVO::getArrivedQuantity).sum();
        if (totalCurrentArrivedQuantity > (totalCount - totalArrivedQuantity)) {
            throw exception(PURCHASE_ORDER_ARRIVED_LOG_EXCEED);
        }

        List<PurchaseOrderItemDO> purchaseOrderItemDOList = purchaseOrderService.getPurchaseOrderItemListByOrderId(purchaseOrderId);
        Map<Long, PurchaseOrderItemDO> purchaseOrderItemMap = CollectionUtils.convertMap(purchaseOrderItemDOList, PurchaseOrderItemDO::getId);
        List<PurchaseOrderArrivedLogDO> orderArrivedLogList = purchaseOrderService.getPurchaseOrderArrivedLogListByPurchaseOrderId(purchaseOrderId);
        Map<Long, PurchaseOrderArrivedLogDO> existArrivedLogMap = CollectionUtils.convertMap(orderArrivedLogList, PurchaseOrderArrivedLogDO::getPurchaseOrderItemId);

        // 逐项检查，单行不得超过对应产品的待到货数量
        for (PurchaseOrderArrivedLogSaveReqVO vo : arrivedLogList) {
            PurchaseOrderItemDO purchaseOrderItemDO = purchaseOrderItemMap.get(vo.getPurchaseOrderItemId());
            PurchaseOrderArrivedLogDO purchaseOrderArrivedLogDO = existArrivedLogMap.get(vo.getPurchaseOrderItemId());

            // 历史到货数量
            int historyArrivedQuantity = purchaseOrderArrivedLogDO == null ? 0 : purchaseOrderArrivedLogDO.getArrivedQuantity();
            if (vo.getArrivedQuantity() > (purchaseOrderItemDO.getPurchaseQuantity() - historyArrivedQuantity)) {
                throw exception(PURCHASE_ORDER_ARRIVED_LOG_EXCEED_ITEM, vo.getArrivedQuantity(),
                        purchaseOrderItemDO.getPlanNumber(),
                        purchaseOrderItemDO.getPurchaseQuantity() - historyArrivedQuantity);
            }
        }

    }

    @Override
    public void updatePurchaseOrderArrivedLog(PurchaseOrderArrivedLogSaveReqVO updateReqVO) {
        // 校验存在
        validatePurchaseOrderArrivedLogExists(updateReqVO.getId());
        // 更新
        PurchaseOrderArrivedLogDO updateObj = BeanUtils.toBean(updateReqVO, PurchaseOrderArrivedLogDO.class);
        purchaseOrderArrivedLogMapper.updateById(updateObj);
    }

    @Override
    public void deletePurchaseOrderArrivedLog(Long id) {
        // 校验存在
        validatePurchaseOrderArrivedLogExists(id);
        // 删除
        purchaseOrderArrivedLogMapper.deleteById(id);
    }

    private void validatePurchaseOrderArrivedLogExists(Long id) {
        if (purchaseOrderArrivedLogMapper.selectById(id) == null) {
            throw exception(PURCHASE_ORDER_ARRIVED_LOG_NOT_EXISTS);
        }
    }

    @Override
    public PurchaseOrderArrivedLogDO getPurchaseOrderArrivedLog(Long id) {
        return purchaseOrderArrivedLogMapper.selectById(id);
    }

    @Override
    public PageResult<PurchaseOrderArrivedLogDO> getPurchaseOrderArrivedLogPage(PurchaseOrderArrivedLogPageReqVO pageReqVO) {
        return purchaseOrderArrivedLogMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PurchaseOrderArrivedLogDO> batchPurchaseOrderArrivedLogByPurchaseItemIds(Collection<Long> purchaseItemIds) {
        LambdaQueryWrapper<PurchaseOrderArrivedLogDO> wrapper = new LambdaQueryWrapperX<PurchaseOrderArrivedLogDO>()
                .inIfPresent(PurchaseOrderArrivedLogDO::getPurchaseOrderItemId, purchaseItemIds)
                .gt(PurchaseOrderArrivedLogDO::getArrivedQuantity, 0);

        return purchaseOrderArrivedLogMapper.selectList(wrapper);
    }
}