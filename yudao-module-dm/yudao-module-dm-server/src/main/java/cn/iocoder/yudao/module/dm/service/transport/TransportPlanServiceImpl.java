package cn.iocoder.yudao.module.dm.service.transport;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.ProductInfoDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.purchaseorderlog.PurchaseOrderArrivedLogDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanDetailDTO;
import cn.iocoder.yudao.module.dm.dal.redis.dao.DmNoRedisDAO;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import cn.iocoder.yudao.module.dm.service.purchase.log.PurchaseOrderArrivedLogService;
import cn.iocoder.yudao.module.dm.service.purchase.order.PurchaseOrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.dm.controller.admin.transport.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.transport.TransportPlanMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.transport.TransportPlanItemMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 头程计划 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class TransportPlanServiceImpl implements TransportPlanService {

    @Resource
    private TransportPlanMapper transportPlanMapper;
    @Resource
    private TransportPlanItemMapper transportPlanItemMapper;
    @Resource
    private DmNoRedisDAO dmNoRedisDAO;
    @Resource
    private PurchaseOrderArrivedLogService purchaseOrderArrivedLogService;
    @Resource
    private ProductInfoService productInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransportPlan(TransportPlanSaveReqVO createReqVO) {

        validateQuantity(createReqVO.getTransportPlanItems());

        // 插入
        TransportPlanDO transportPlan = BeanUtils.toBean(createReqVO, TransportPlanDO.class);
        List<TransportPlanItemDO> transportPlanItems = createReqVO.getTransportPlanItems();
        List<String> checkInIds = convertList(transportPlanItems, TransportPlanItemDO::getOverseaLocationCheckinId)
                .stream().distinct().collect(Collectors.toList());

        transportPlan.setCode(dmNoRedisDAO.generate(DmNoRedisDAO.TRANSPORT_PLAN_NO_PREFIX));
        transportPlan.setOverseaLocationCheckinId(checkInIds);
        transportPlanMapper.insert(transportPlan);

        // 插入子表
        createTransportPlanItemList(transportPlan.getId(), transportPlanItems);
        // 返回
        return transportPlan.getId();
    }

    private void validateQuantity(List<TransportPlanItemDO> transportPlanItems) {
        List<Long> purchaseItemIds = convertList(transportPlanItems, TransportPlanItemDO::getPurchaseOrderItemId);
        List<PurchaseOrderArrivedLogDO> arrivedLogDOList = purchaseOrderArrivedLogService.batchPurchaseOrderArrivedLogByPurchaseItemIds(purchaseItemIds);

        // 将List转换为Map<purchaseOrderItemId, 累计arrivedQuantity>
        Map<Long, Integer> arrivedQuantityMap = arrivedLogDOList.stream()
                .collect(Collectors.groupingBy(
                        PurchaseOrderArrivedLogDO::getPurchaseOrderItemId,
                        Collectors.summingInt(PurchaseOrderArrivedLogDO::getArrivedQuantity)
                ));

        for (TransportPlanItemDO transportPlanItem : transportPlanItems) {
            if (Objects.isNull(transportPlanItem.getPurchaseOrderItemId())) {
                continue;
            }
            // 凡是关联了采购订单项，则需要校验发货数量是否大于采购订单项的累计到货数量
            Long purchaseOrderItemId = transportPlanItem.getPurchaseOrderItemId();
            if (transportPlanItem.getQuantity() > arrivedQuantityMap.getOrDefault(purchaseOrderItemId, 0)) {
                ProductInfoDO productInfo = productInfoService.getProductInfo(transportPlanItem.getProductId());
                throw exception(TRANSPORT_PLAN_ITEM_QUANTITY_NOT_ENOUGH, productInfo.getSkuId(), transportPlanItem.getQuantity(),
                        arrivedQuantityMap.getOrDefault(purchaseOrderItemId, 0));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransportPlan(TransportPlanSaveReqVO updateReqVO) {
        // 校验存在
        validateTransportPlanExists(updateReqVO.getId());
        validateQuantity(updateReqVO.getTransportPlanItems());
        // 更新
        TransportPlanDO updateObj = BeanUtils.toBean(updateReqVO, TransportPlanDO.class);
        List<TransportPlanItemDO> transportPlanItems = updateReqVO.getTransportPlanItems();
        List<String> checkInIds = convertList(transportPlanItems, TransportPlanItemDO::getOverseaLocationCheckinId)
                .stream().distinct().collect(Collectors.toList());
        updateObj.setOverseaLocationCheckinId(checkInIds);

        transportPlanMapper.updateById(updateObj);

        // 更新子表
        updateTransportPlanItemList(updateReqVO.getId(), updateReqVO.getTransportPlanItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransportPlan(Long id) {
        // 校验存在
        validateTransportPlanExists(id);
        // 删除
//        transportPlanMapper.deleteById(id);

        // 作废
        transportPlanMapper.updateById(new TransportPlanDO().setId(id).setTransportStatus(99));

        // 删除子表
        deleteTransportPlanItemByPlanId(id);
    }

    private void validateTransportPlanExists(Long id) {
        if (transportPlanMapper.selectById(id) == null) {
            throw exception(TRANSPORT_PLAN_NOT_EXISTS);
        }
    }

    @Override
    public TransportPlanDO getTransportPlan(Long id) {
        return transportPlanMapper.selectById(id);
    }

    @Override
    public PageResult<TransportPlanDO> getTransportPlanPage(TransportPlanPageReqVO pageReqVO) {
        return transportPlanMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（头程计划明细） ====================

    @Override
    public List<TransportPlanItemDO> getTransportPlanItemListByPlanId(Long planId) {
        return transportPlanItemMapper.selectListByPlanId(planId);
    }

    @Override
    public List<TransportPlanItemDO> getTransportPlanItemListByProductId(Long productId) {
        return transportPlanItemMapper.selectList(TransportPlanItemDO::getProductId, productId);
    }

    @Override
    public List<TransportPlanItemDO> batchTransportPlanItemListByPurchaseOrderItemIds(Collection<Long> purchaseOrderItemIds) {
        return transportPlanItemMapper.selectList(TransportPlanItemDO::getPurchaseOrderItemId, purchaseOrderItemIds);
    }

    @Override
    public TransportPlanItemDO getTransportPlanItemByPurchaseOrderItemId(Long purchaseOrderItemId) {
        return transportPlanItemMapper.selectOne(TransportPlanItemDO::getPurchaseOrderItemId, purchaseOrderItemId);
    }

    @Override
    public List<TransportPlanItemDO> batchTransportPlanItemByPurchaseOrderItemId(Collection<Long> purchaseOrderItemIds) {
        return transportPlanItemMapper.selectList(TransportPlanItemDO::getPurchaseOrderItemId, purchaseOrderItemIds);
    }

    @Override
    public List<TransportPlanItemDO> getTransportPlanItemListByProductIdsAndDateRange(Collection<Long> productIds,
                                                                                     LocalDateTime beginTime,
                                                                                     LocalDateTime endTime) {
        return transportPlanItemMapper.selectListByProductIdsAndDateRange(productIds, beginTime, endTime);
    }

    private void createTransportPlanItemList(Long planId, List<TransportPlanItemDO> list) {
        list.forEach(o -> o.setPlanId(planId));
        transportPlanItemMapper.insertBatch(list);
    }

    private void updateTransportPlanItemList(Long planId, List<TransportPlanItemDO> list) {
        deleteTransportPlanItemByPlanId(planId);
        list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createTransportPlanItemList(planId, list);
    }

    private void deleteTransportPlanItemByPlanId(Long planId) {
        transportPlanItemMapper.deleteByPlanId(planId);
    }

    @Override
    public List<TransportPlanDetailDTO> getTransportPlanDetailList(TransportPlanDetailReqVO reqVO) {
        return transportPlanMapper.selectTransportPlanDetailList(reqVO);
    }

}