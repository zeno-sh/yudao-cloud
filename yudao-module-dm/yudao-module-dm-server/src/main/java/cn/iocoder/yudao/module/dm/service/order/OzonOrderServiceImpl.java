package cn.iocoder.yudao.module.dm.service.order;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.ProductVolumeReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderSalesStatsDO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.utils.DmDateUtils;
import cn.iocoder.yudao.module.dm.infrastructure.service.dto.ProductVolumeDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.dm.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.order.OzonOrderItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.order.OzonOrderMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.order.OzonOrderItemMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * Ozon订单 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class OzonOrderServiceImpl implements OzonOrderService {

    @Resource
    private OzonOrderMapper ozonOrderMapper;
    @Resource
    private OzonOrderItemMapper ozonOrderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOzonOrder(OzonOrderSaveReqVO createReqVO) {
        // 插入
        OzonOrderDO ozonOrder = BeanUtils.toBean(createReqVO, OzonOrderDO.class);
        ozonOrderMapper.insert(ozonOrder);

        // 插入子表
//        createOzonOrderItemList(ozonOrder.getOrderId(), createReqVO.getOzonOrderItems());
        // 返回
        return ozonOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOzonOrder(OzonOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateOzonOrderExists(updateReqVO.getId());
        // 更新
        OzonOrderDO updateObj = BeanUtils.toBean(updateReqVO, OzonOrderDO.class);
        ozonOrderMapper.updateById(updateObj);

        // 更新子表
        if (CollectionUtils.isNotEmpty(updateReqVO.getOzonOrderItems())) {
            updateOzonOrderItemList(updateReqVO.getOrderId(), updateReqVO.getOzonOrderItems());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOzonOrder(Long id) {
        // 校验存在
        validateOzonOrderExists(id);
        // 删除
        ozonOrderMapper.deleteById(id);

        OzonOrderDO ozonOrderDO = ozonOrderMapper.selectById(id);

        // 删除子表
        deleteOzonOrderItemByOrderId(ozonOrderDO.getOrderId());
    }

    private void validateOzonOrderExists(Long id) {
        if (ozonOrderMapper.selectById(id) == null) {
            throw exception(OZON_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public OzonOrderDO getOzonOrder(Long id) {
        return ozonOrderMapper.selectById(id);
    }

    @Override
    public PageResult<OzonOrderDO> getOzonOrderPage(OzonOrderPageReqVO pageReqVO) {
        return ozonOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public IPage<OzonOrderDO> getOzonOrderPage2(IPage<OzonOrderDO> page, OzonOrderPageReqVO reqVO) {
        // 时区转换 -> 莫斯科时区
        LocalDateTime[] inProcessAtMoscowLocalDateTimes = DmDateUtils.convertVODateArrayToUtcTimeRange(reqVO.getInProcessAt());
        reqVO.setInProcessAtParams(inProcessAtMoscowLocalDateTimes);

//        if (StringUtils.isNotBlank(reqVO.getTodayShipmentDate())) {
//            // 将日期字符串转换为 LocalDate 对象
//            LocalDate shipmentDate = LocalDate.parse(reqVO.getTodayShipmentDate());
//
//            // 获取当天的开始时间和结束时间
//            LocalDateTime[] shipmentDateParams = new LocalDateTime[2];
//            shipmentDateParams[0] = shipmentDate.atStartOfDay();
//            shipmentDateParams[1] = shipmentDate.atTime(LocalTime.MAX);
//            reqVO.setShipmentDateParams(shipmentDateParams);
//        }

        if (null != reqVO.getShipmentDate() && reqVO.getShipmentDate().length > 0) {
            reqVO.setShipmentDateParams(reqVO.getShipmentDate());
        }
        return ozonOrderMapper.selectPage2(page, reqVO);
    }

    @Override
    public List<OzonOrderDO> getOzonOrderList(String clientId, String postingNumber, LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapperX<OzonOrderDO> wrapperX = new LambdaQueryWrapperX<OzonOrderDO>()
                .eqIfPresent(OzonOrderDO::getClientId, clientId)
                .eqIfPresent(OzonOrderDO::getPostingNumber, postingNumber)
                .betweenIfPresent(OzonOrderDO::getInProcessAt, beginTime, endTime)
                .orderByDesc(OzonOrderDO::getInProcessAt);
        return ozonOrderMapper.selectList(wrapperX);
    }

    @Override
    public List<OzonOrderDO> getOzonOrderListByPlatformOrderId(String clientId, String platformOrderId, String postingNumber, LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapperX<OzonOrderDO> wrapperX = new LambdaQueryWrapperX<OzonOrderDO>()
                .eqIfPresent(OzonOrderDO::getClientId, clientId)
                .eqIfPresent(OzonOrderDO::getOrderId, platformOrderId)
                .eqIfPresent(OzonOrderDO::getPostingNumber, postingNumber)
                .betweenIfPresent(OzonOrderDO::getInProcessAt, beginTime, endTime)
                .orderByDesc(OzonOrderDO::getInProcessAt);
        return ozonOrderMapper.selectList(wrapperX);
    }

    @Override
    public List<OzonOrderDO> getOzonOrderList(String[] clientIds, LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapperX<OzonOrderDO> wrapperX = new LambdaQueryWrapperX<OzonOrderDO>()
                .inIfPresent(OzonOrderDO::getClientId, clientIds)
                .betweenIfPresent(OzonOrderDO::getInProcessAt, beginTime, endTime)
                .orderByDesc(OzonOrderDO::getInProcessAt);
        return ozonOrderMapper.selectList(wrapperX);
    }

    @Override
    public List<OzonOrderDO> getOrderList(String clientId, Collection<String> platformOrderIds) {
        LambdaQueryWrapperX<OzonOrderDO> wrapperX = new LambdaQueryWrapperX<OzonOrderDO>()
                .eqIfPresent(OzonOrderDO::getClientId, clientId)
                .inIfPresent(OzonOrderDO::getOrderId, platformOrderIds)
                .orderByDesc(OzonOrderDO::getInProcessAt);
        return ozonOrderMapper.selectList(wrapperX);
    }

    @Override
    public List<OzonOrderDO> batchOrderList(Collection<Long> ids) {
        return ozonOrderMapper.selectList(OzonOrderDO::getId, ids);
    }

    @Override
    public List<OzonOrderDO> batchOrderListByPostingNumbers(Collection<String> postingNumbers) {
        return ozonOrderMapper.selectList(OzonOrderDO::getPostingNumber, postingNumbers);
    }

    @Override
    public List<OzonOrderDO> batchOrderListByOrderNumbers(Collection<String> clientIds, Collection<String> orderNumbers) {
        LambdaQueryWrapperX<OzonOrderDO> wrapperX = new LambdaQueryWrapperX<OzonOrderDO>()
                .inIfPresent(OzonOrderDO::getClientId, clientIds)
                .inIfPresent(OzonOrderDO::getOrderNumber, orderNumbers);
        return ozonOrderMapper.selectList(wrapperX);
    }

    // ==================== 子表（订单商品详情） ====================

    @Override
    public List<OzonOrderItemDO> getOrderItemListByOrderIdAndPostingNumber(String clientId, String orderId, String postingNumber) {
        return ozonOrderItemMapper.selectList(new LambdaQueryWrapperX<OzonOrderItemDO>()
                .eqIfPresent(OzonOrderItemDO::getClientId, clientId)
                .eqIfPresent(OzonOrderItemDO::getOrderId, orderId)
                .eqIfPresent(OzonOrderItemDO::getPostingNumber, postingNumber));
    }

    @Override
    public List<OzonOrderItemDO> getOrderItemList(String clientId, String postingNumber, String offerId) {
        LambdaQueryWrapperX<OzonOrderItemDO> wrapperX = new LambdaQueryWrapperX<OzonOrderItemDO>()
                .eqIfPresent(OzonOrderItemDO::getClientId, clientId)
                .eqIfPresent(OzonOrderItemDO::getPostingNumber, postingNumber)
                .eqIfPresent(OzonOrderItemDO::getOfferId, offerId)
                .orderByDesc(OzonOrderItemDO::getInProcessAt);
        return ozonOrderItemMapper.selectList(wrapperX);
    }

    @Override
    public List<OzonOrderItemDO> getOrderItemListByPostingNumberAndPlatformSkuId(String clientId, String postingNumber, String platformSkuId) {
        LambdaQueryWrapperX<OzonOrderItemDO> wrapperX = new LambdaQueryWrapperX<OzonOrderItemDO>()
                .eqIfPresent(OzonOrderItemDO::getClientId, clientId)
                .eqIfPresent(OzonOrderItemDO::getPostingNumber, postingNumber)
                .eqIfPresent(OzonOrderItemDO::getPlatformSkuId, platformSkuId)
                .orderByDesc(OzonOrderItemDO::getInProcessAt);
        return ozonOrderItemMapper.selectList(wrapperX);
    }

    @Override
    public List<OzonOrderItemDO> getOzonOrderItemListByClientId(String[] clientIds, LocalDateTime[] dateTimes) {
        return ozonOrderItemMapper.selectList(new LambdaQueryWrapperX<OzonOrderItemDO>()
                .inIfPresent(OzonOrderItemDO::getClientId, Arrays.asList(clientIds))
                .betweenIfPresent(OzonOrderItemDO::getInProcessAt, dateTimes[0], dateTimes[1])
        );
    }

    @Override
    public List<OzonOrderItemDO> batchOrderItemListByPostingNumbers(String[] clientIds, Collection<String> postingNumbers) {
        LambdaQueryWrapperX<OzonOrderItemDO> wrapperX = new LambdaQueryWrapperX<OzonOrderItemDO>()
                .inIfPresent(OzonOrderItemDO::getClientId, clientIds)
                .inIfPresent(OzonOrderItemDO::getPostingNumber, postingNumbers);
        return ozonOrderItemMapper.selectList(wrapperX);
    }

    @Override
    public void createOzonOrderItemList(String orderId, List<OzonOrderItemDO> list) {
        list.forEach(o -> o.setOrderId(orderId));
        ozonOrderItemMapper.insertBatch(list);
    }

    @Override
    public void updateOzonOrderItemList(String orderId, List<OzonOrderItemDO> list) {
//        deleteOzonOrderItemByOrderId(orderId);
//		list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
//        createOzonOrderItemList(orderId, list);
        ozonOrderItemMapper.updateBatch(list);
    }

    @Override
    public PageResult<OzonOrderItemDO> getOrderItemPage(ProductVolumeReqVO reqVO) {
        return ozonOrderItemMapper.selectItemPage(reqVO);
    }

    @Override
    public PageResult<OzonOrderItemDO> getSimpleOrderItemPage(ProductVolumeReqVO reqVO) {
        return ozonOrderItemMapper.selectSimplePage(reqVO);
    }

    @Override
    public PageResult<OzonOrderItemDO> getShopOrderItemPage(ProductVolumeReqVO reqVO) {
        // 时区转换 -> 莫斯科时区
        LocalDateTime[] inProcessAtMoscowLocalDateTimes = DmDateUtils.convertVODateArrayToUtcTimeRange(reqVO.getDate());
        
        // 如果时间范围为空，直接返回空结果
        if (inProcessAtMoscowLocalDateTimes == null) {
            return new PageResult<>();
        }
        
        // 创建 MyBatis-Plus 分页对象
        IPage<OzonOrderItemDO> mpPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                reqVO.getPageNo(), reqVO.getPageSize());
        
        // 执行分页查询
        mpPage = ozonOrderItemMapper.selectShopOrderItemPage(
                mpPage,
                reqVO, 
                inProcessAtMoscowLocalDateTimes[0], 
                inProcessAtMoscowLocalDateTimes[1]);
        
        // 转换为 PageResult 对象
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    @Override
    public PageResult<OzonOrderItemDO> getSkuOrderItemPage(ProductVolumeReqVO reqVO) {
        // 时区转换 -> 莫斯科时区
        LocalDateTime[] inProcessAtMoscowLocalDateTimes = DmDateUtils.convertVODateArrayToUtcTimeRange(reqVO.getDate());
        
        // 如果时间范围为空，直接返回空结果
        if (inProcessAtMoscowLocalDateTimes == null) {
            return new PageResult<>();
        }
        
        // 创建 MyBatis-Plus 分页对象
        IPage<OzonOrderItemDO> mpPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                reqVO.getPageNo(), reqVO.getPageSize());
        
        // 执行分页查询
        mpPage = ozonOrderItemMapper.selectSkuOrderItemPage(
                mpPage,
                reqVO, 
                inProcessAtMoscowLocalDateTimes[0], 
                inProcessAtMoscowLocalDateTimes[1]);
        
        // 转换为 PageResult 对象
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    @Override
    public List<ProductVolumeDTO> getSkuOrderItem(ProductVolumeReqVO reqVO) {

        // 时区转换 -> 莫斯科时区
        LocalDateTime[] inProcessAtMoscowLocalDateTimes = DmDateUtils.convertVODateArrayToUtcTimeRange(reqVO.getDate());

        // 如果时间范围为空，直接返回空结果
        if (inProcessAtMoscowLocalDateTimes == null) {
            return Collections.emptyList();
        }

        return ozonOrderItemMapper.selectSkuOrderItem(
                reqVO,
                inProcessAtMoscowLocalDateTimes[0],
                inProcessAtMoscowLocalDateTimes[1]
        );
    }

    private void deleteOzonOrderItemByOrderId(String orderId) {
        ozonOrderItemMapper.deleteByOrderId(orderId);
    }

    @Override
    public Map<String, Integer> getMonthSalesQuantityMap(String[] clientIds, String month, String offerId) {
        List<OzonOrderSalesStatsDO> salesStats = ozonOrderMapper.selectMonthSalesStats(clientIds, month, offerId);
        Map<String, Integer> result = new HashMap<>();
        for (OzonOrderSalesStatsDO stat : salesStats) {
            result.put(stat.getOfferId(), stat.getSalesQuantity());
        }
        return result;
    }

    @Override
    public Map<String, Integer> getHistorySalesQuantityMap(String[] clientIds, String month, String offerId) {
        List<OzonOrderSalesStatsDO> salesStats = ozonOrderMapper.selectHistorySalesStats(clientIds, month, offerId);
        Map<String, Integer> result = new HashMap<>();
        for (OzonOrderSalesStatsDO stat : salesStats) {
            result.put(stat.getOfferId(), stat.getSalesQuantity());
        }
        return result;
    }

    @Override
    public void deleteOzonOrderItemByPostingNumberAndPlatformSkuId(String clientId, String postingNumber, String platformSkuId) {
        LambdaQueryWrapperX<OzonOrderItemDO> wrapperX = new LambdaQueryWrapperX<OzonOrderItemDO>()
                .eq(OzonOrderItemDO::getClientId, clientId)
                .eq(OzonOrderItemDO::getPostingNumber, postingNumber)
                .eq(OzonOrderItemDO::getPlatformSkuId, platformSkuId);
        ozonOrderItemMapper.delete(wrapperX);
    }

}