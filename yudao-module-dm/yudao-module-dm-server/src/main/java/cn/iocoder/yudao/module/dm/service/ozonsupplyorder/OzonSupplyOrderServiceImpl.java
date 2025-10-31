package cn.iocoder.yudao.module.dm.service.ozonsupplyorder;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonsupplyorder.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.product.SupplierPriceOfferDO;
import cn.iocoder.yudao.module.dm.dal.mysql.ozonsupplyorder.OzonSupplyOrderItemMapper;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.SupplyOrderManagerService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import cn.iocoder.yudao.module.dm.service.product.ProductInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.ozonsupplyorder.OzonSupplyOrderMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 供应订单 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
@Slf4j
public class OzonSupplyOrderServiceImpl implements OzonSupplyOrderService {

    @Resource
    private OzonSupplyOrderMapper ozonSupplyOrderMapper;
    @Resource
    private OzonSupplyOrderItemMapper ozonSupplyOrderItemMapper;
    @Resource
    private OzonShopMappingService ozonShopMappingService;
    @Resource
    private SupplyOrderManagerService supplyOrderManagerService;
    @Resource
    private ProductInfoService productInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOzonSupplyOrder(OzonSupplyOrderSaveReqVO createReqVO) {

        validateOzonSupplyOrderExist(createReqVO.getSupplyOrderId());
        // 插入
        OzonSupplyOrderDO ozonSupplyOrder = BeanUtils.toBean(createReqVO, OzonSupplyOrderDO.class);
        ozonSupplyOrder.setUpdatedManually(Boolean.TRUE);
        ozonSupplyOrderMapper.insert(ozonSupplyOrder);

        // 插入子表
        createOzonSupplyOrderItemList(ozonSupplyOrder.getId(), ozonSupplyOrder.getClientId(), createReqVO.getOzonSupplyOrderItems());
        // 返回
        return ozonSupplyOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOzonSupplyOrder(OzonSupplyOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateOzonSupplyOrderExists(updateReqVO.getId());
        // 更新
        OzonSupplyOrderDO updateObj = BeanUtils.toBean(updateReqVO, OzonSupplyOrderDO.class);
        updateObj.setUpdatedManually(Boolean.TRUE);
        ozonSupplyOrderMapper.updateById(updateObj);

        // 更新子表
        updateOzonSupplyOrderItemList(updateReqVO.getId(), updateReqVO.getClientId(), updateReqVO.getOzonSupplyOrderItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOzonSupplyOrder(Long id) {
        // 校验存在
        validateOzonSupplyOrderExists(id);
        // 删除
        ozonSupplyOrderMapper.deleteById(id);

        // 删除子表
        deleteOzonSupplyOrderItemBySupplyOrderId(id);
    }

    private void validateOzonSupplyOrderExists(Long id) {
        if (ozonSupplyOrderMapper.selectById(id) == null) {
            throw exception(OZON_SUPPLY_ORDER_NOT_EXISTS);
        }
    }

    private void validateOzonSupplyOrderExist(Long supplyOrderId) {
        if (ozonSupplyOrderMapper.selectOne(OzonSupplyOrderDO::getSupplyOrderId, supplyOrderId) != null) {
            throw exception(OZON_SUPPLY_ORDER_EXISTS);
        }
    }

    @Override
    public OzonSupplyOrderDO getOzonSupplyOrder(Long id) {
        return ozonSupplyOrderMapper.selectById(id);
    }

    @Override
    public PageResult<OzonSupplyOrderDO> getOzonSupplyOrderPage(OzonSupplyOrderPageReqVO pageReqVO) {
        return ozonSupplyOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OzonSupplyOrderStatsDO> getOzonSupplyOrderStats(String clientId, Collection<Long> supplyOrderIds) {
        return ozonSupplyOrderMapper.selectOrderStatsBatch(clientId, supplyOrderIds);
    }

    @Override
    public void syncSupplyOrderItems(Long supplyOrderId) {
        // 1. 获取订单信息
        OzonSupplyOrderDO order = ozonSupplyOrderMapper.selectOne(OzonSupplyOrderDO::getSupplyOrderId, supplyOrderId);
        if (order == null) {
            log.error("[syncSupplyOrderItems][订单不存在] supplyOrderId: {}", supplyOrderId);
            return;
        }

        // 2. 获取API Key
        String clientId = order.getClientId();
        OzonShopMappingDO shopMapping = ozonShopMappingService.getOzonShopMappingByClientId(clientId);
        if (shopMapping == null) {
            log.error("[syncSupplyOrderItems][店铺不存在] clientId: {}", clientId);
            return;
        }

        // 3. 先调用 /v2/supply-order/get 更新订单信息
        supplyOrderManagerService.asyncSyncSupplyOrderDetails(clientId, shopMapping.getApiKey(), 
                Collections.singletonList(String.valueOf(supplyOrderId)));

        // 4. 再调用 /v1/supply-order/bundle 更新商品信息
        supplyOrderManagerService.asyncSyncSupplyOrderItems(clientId, shopMapping.getApiKey(), 
                String.valueOf(supplyOrderId));
    }

    // ==================== 子表（供应订单商品） ====================

    @Override
    public List<OzonSupplyOrderItemDO> getOzonSupplyOrderItemListBySupplyOrderId(Long supplyOrderId) {
        return ozonSupplyOrderItemMapper.selectListBySupplyOrderId(supplyOrderId);
    }

    private void createOzonSupplyOrderItemList(Long supplyOrderId, List<OzonSupplyOrderItemDO> list) {
        list.forEach(o -> o.setSupplyOrderId(supplyOrderId));
        ozonSupplyOrderItemMapper.insertBatch(list);
    }

    private void createOzonSupplyOrderItemList(Long supplyOrderId, String clientId, List<OzonSupplyOrderItemDO> list) {
        list.forEach(o -> {
            o.setSupplyOrderId(supplyOrderId);
            o.setClientId(clientId);
        });
        ozonSupplyOrderItemMapper.insertBatch(list);
    }

    private void updateOzonSupplyOrderItemList(Long supplyOrderId, String clientId, List<OzonSupplyOrderItemDO> list) {
        deleteOzonSupplyOrderItemBySupplyOrderId(supplyOrderId);
		list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createOzonSupplyOrderItemList(supplyOrderId, clientId, list);
    }

    private void deleteOzonSupplyOrderItemBySupplyOrderId(Long supplyOrderId) {
        ozonSupplyOrderItemMapper.deleteBySupplyOrderId(supplyOrderId);
    }

    @Override
    public PageResult<OzonFboInboundReportRespVO> getFboInboundReportPage(OzonFboInboundReportReqVO reqVO) {
        // 1. 解析月份，获取月初和月末
        LocalDate monthStart = LocalDate.parse(reqVO.getMonth() + "-01");
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        Date monthBeginTime = DateUtil.date(monthStart.atStartOfDay());
        Date monthEndTime = DateUtil.date(monthEnd.atTime(23, 59, 59));

        // 设置历史日期范围：从上个月初到本月初
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        Date historyBeginTime = DateUtil.date(lastMonthStart.atStartOfDay());

        log.info("[getFboInboundReportPage][当前月份: {}][查询范围: {} ~ {}][历史范围: {} ~ {}]",
                reqVO.getMonth(), monthBeginTime, monthEndTime, historyBeginTime, monthBeginTime);

        // 2. 查询当月进仓数据
        IPage<OzonFboInboundStatsDO> monthlyPage = ozonSupplyOrderMapper.selectFboInboundPage(
                new Page<>(reqVO.getPageNo(), reqVO.getPageSize()),
                reqVO, monthBeginTime, monthEndTime);
        if (CollectionUtils.isEmpty(monthlyPage.getRecords())) {
            return new PageResult<>(Collections.emptyList(), monthlyPage.getTotal());
        }

        // 3. 获取所有本地产品ID
        List<Long> dmProductIdList = convertList(monthlyPage.getRecords(), OzonFboInboundStatsDO::getDmProductId);

        // 4. 查询历史进仓和销售数据
        List<OzonFboInboundStatsDO> historyInboundStats = ozonSupplyOrderMapper.selectFboInboundStats(
                historyBeginTime, monthBeginTime, reqVO.getClientIds(), dmProductIdList);
        Map<Long, Integer> historyInboundMap = new HashMap<>();
        for (OzonFboInboundStatsDO stat : historyInboundStats) {
            historyInboundMap.put(stat.getDmProductId(), stat.getInboundQuantity());
        }

        List<OzonFboSalesStatsDO> historySalesStats = ozonSupplyOrderMapper.selectFboSalesStats(
                historyBeginTime, monthBeginTime, reqVO.getClientIds(), dmProductIdList);

        // key: dmProductId -> salesQuantity
        Map<Long, Integer> historySalesMap = new HashMap<>();
        for (OzonFboSalesStatsDO stat : historySalesStats) {
            historySalesMap.put(stat.getDmProductId(), stat.getSalesQuantity());
        }

        // 5. 查询当月销售数据
        List<OzonFboSalesStatsDO> salesStats = ozonSupplyOrderMapper.selectFboSalesStats(
                monthBeginTime, monthEndTime, reqVO.getClientIds(), dmProductIdList);
        // key: dmProductId -> salesQuantity
        Map<Long, Integer> salesMap = new HashMap<>();
        for (OzonFboSalesStatsDO stat : salesStats) {
            salesMap.put(stat.getDmProductId(), stat.getSalesQuantity());
        }


        // 7. 查询产品的供应商报价信息
        Map<Long, SupplierPriceOfferDO> priceMap = productInfoService.getSupplierPriceOfferMapByProductIds(dmProductIdList);

        Map<Long, ProductSimpleInfoVO> productInfoMap = productInfoService.batchQueryProductSimpleInfo(dmProductIdList);

        // 8. 组装返回数据
        List<OzonFboInboundReportRespVO> resultList = new ArrayList<>();
        for (OzonFboInboundStatsDO inbound : monthlyPage.getRecords()) {

            if (null == inbound.getDmProductId()) {
                log.warn("[getFboInboundReportPage][没有找到对应的产品信息][dmProductId 为空]");
                continue;
            }

            Long dmProductId = inbound.getDmProductId();

            // 获取历史数据
            int historyInbound = historyInboundMap.getOrDefault(dmProductId, 0);
            int historySales = historySalesMap.getOrDefault(dmProductId, 0);
            
            // 计算期初结余
            int initialBalance = historyInbound - historySales;
            
            // 获取当月销售数据
            int salesQuantity = salesMap.getOrDefault(dmProductId, 0);
            
            // 计算期末结余
            int finalBalance = initialBalance + inbound.getInboundQuantity() - salesQuantity;


            SupplierPriceOfferDO priceOffer = priceMap.get(dmProductId);
            ProductSimpleInfoVO productSimpleInfoVO = productInfoMap.get(dmProductId);

            // 计算含税和不含税金额
            BigDecimal taxIncludedAmount = BigDecimal.ZERO;
            BigDecimal taxExcludedAmount = BigDecimal.ZERO;
            if (priceOffer != null) {
                taxIncludedAmount = calculateTaxIncludedAmount(priceOffer.getPrice(), priceOffer.getTaxRate(), inbound.getInboundQuantity());
                taxExcludedAmount = calculateTaxExcludedAmount(priceOffer.getPrice(), inbound.getInboundQuantity());
            }

            // 创建VO对象
            OzonFboInboundReportRespVO vo = new OzonFboInboundReportRespVO()
                    .setMonth(reqVO.getMonth())
                    .setProductSimpleInfo(productSimpleInfoVO)
                    .setInitialBalance(initialBalance)
                    .setInboundQuantity(inbound.getInboundQuantity())
                    .setSalesQuantity(salesQuantity)
                    .setFinalBalance(finalBalance)
                    .setTaxIncludedAmount(taxIncludedAmount)
                    .setTaxExcludedAmount(taxExcludedAmount);

            if (priceOffer != null) {
                vo.setSupplierPrice(priceOffer.getPrice());
            }

            resultList.add(vo);
        }

        return new PageResult<>(resultList, monthlyPage.getTotal());
    }

    private BigDecimal calculateTaxIncludedAmount(BigDecimal price, BigDecimal taxRate, int quantity) {
        return price.multiply(BigDecimal.ONE.add(taxRate))
                   .multiply(BigDecimal.valueOf(quantity))
                   .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTaxExcludedAmount(BigDecimal price, int quantity) {
        return price.multiply(BigDecimal.valueOf(quantity))
                   .setScale(2, RoundingMode.HALF_UP);
    }

}