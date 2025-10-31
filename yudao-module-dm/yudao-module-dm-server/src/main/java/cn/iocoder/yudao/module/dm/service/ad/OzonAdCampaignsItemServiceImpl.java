package cn.iocoder.yudao.module.dm.service.ad;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;
import cn.iocoder.yudao.module.dm.dal.mysql.ad.OzonAdCampaignsItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 广告明细 Service 实现类
 *
 * @author Zeno
 */
@Service
@Slf4j
public class OzonAdCampaignsItemServiceImpl implements OzonAdCampaignsItemService {

    @Resource
    private OzonAdCampaignsItemMapper ozonAdCampaignsItemMapper;

    @Override
    public Map<String, OzonAdCampaignsItemDO> getAdCostByInProcessDateAndSku(
            List<String> clientIds, 
            List<String> platformSkuIds, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        Map<String, OzonAdCampaignsItemDO> resultMap = new HashMap<>();
        
        // 调用Mapper查询广告费用数据
        LambdaQueryWrapperX<OzonAdCampaignsItemDO> queryWrapper = new LambdaQueryWrapperX<OzonAdCampaignsItemDO>()
                .inIfPresent(OzonAdCampaignsItemDO::getClientId, clientIds)
                .betweenIfPresent(OzonAdCampaignsItemDO::getDate, startDate, endDate);


        // 调用Mapper查询广告费用数据
        List<OzonAdCampaignsItemDO> adItems = ozonAdCampaignsItemMapper.selectList(queryWrapper);

        // 按clientId_platformSkuId分组聚合
        for (OzonAdCampaignsItemDO item : adItems) {
            // 防止空指针异常
            if (item.getClientId() == null || item.getPlatformSkuId() == null) {
                log.warn("跳过无效数据：clientId={}, platformSkuId={}", item.getClientId(), item.getPlatformSkuId());
                continue;
            }
            
            String key = item.getClientId() + "_" + item.getPlatformSkuId();
            
            if (resultMap.containsKey(key)) {
                // 聚合数据
                OzonAdCampaignsItemDO existing = resultMap.get(key);
                // 防止BigDecimal为null的情况
                BigDecimal existingMoneySpent = existing.getMoneySpent() != null ? existing.getMoneySpent() : BigDecimal.ZERO;
                BigDecimal itemMoneySpent = item.getMoneySpent() != null ? item.getMoneySpent() : BigDecimal.ZERO;
                existing.setMoneySpent(existingMoneySpent.add(itemMoneySpent));
                
                BigDecimal existingOrdersMoney = existing.getOrdersMoney() != null ? existing.getOrdersMoney() : BigDecimal.ZERO;
                BigDecimal itemOrdersMoney = item.getOrdersMoney() != null ? item.getOrdersMoney() : BigDecimal.ZERO;
                existing.setOrdersMoney(existingOrdersMoney.add(itemOrdersMoney));
                
                Integer existingOrders = existing.getOrders() != null ? existing.getOrders() : 0;
                Integer itemOrders = item.getOrders() != null ? item.getOrders() : 0;
                existing.setOrders(existingOrders + itemOrders);
                
                Integer existingViews = existing.getViews() != null ? existing.getViews() : 0;
                Integer itemViews = item.getViews() != null ? item.getViews() : 0;
                existing.setViews(existingViews + itemViews);
                
                Integer existingClicks = existing.getClicks() != null ? existing.getClicks() : 0;
                Integer itemClicks = item.getClicks() != null ? item.getClicks() : 0;
                existing.setClicks(existingClicks + itemClicks);
            } else {
                resultMap.put(key, item);
            }
        }
        
        log.info("查询SKU维度广告费用完成，查询条件：clientIds={}, platformSkuIds={}, startDate={}, endDate={}, 结果数量={}", 
                clientIds, platformSkuIds, startDate, endDate, resultMap.size());
        
        return resultMap;
    }

    @Override
    public Map<String, OzonAdCampaignsItemDO> getAdCostByInProcessDateForClient(
            List<String> clientIds, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        Map<String, OzonAdCampaignsItemDO> resultMap = new HashMap<>();

        LambdaQueryWrapperX<OzonAdCampaignsItemDO> queryWrapper = new LambdaQueryWrapperX<OzonAdCampaignsItemDO>()
                .inIfPresent(OzonAdCampaignsItemDO::getClientId, clientIds)
                .betweenIfPresent(OzonAdCampaignsItemDO::getDate, startDate, endDate);

        
        // 调用Mapper查询广告费用数据
        List<OzonAdCampaignsItemDO> adItems = ozonAdCampaignsItemMapper.selectList(queryWrapper);
        
        // 按clientId分组聚合
        for (OzonAdCampaignsItemDO item : adItems) {
            // 防止空指针异常
            if (item.getClientId() == null) {
                log.warn("跳过无效数据：clientId={}", item.getClientId());
                continue;
            }
            
            String key = item.getClientId();
            
            if (resultMap.containsKey(key)) {
                // 聚合数据
                OzonAdCampaignsItemDO existing = resultMap.get(key);
                // 防止BigDecimal为null的情况
                BigDecimal existingMoneySpent = existing.getMoneySpent() != null ? existing.getMoneySpent() : BigDecimal.ZERO;
                BigDecimal itemMoneySpent = item.getMoneySpent() != null ? item.getMoneySpent() : BigDecimal.ZERO;
                existing.setMoneySpent(existingMoneySpent.add(itemMoneySpent));
                
                BigDecimal existingOrdersMoney = existing.getOrdersMoney() != null ? existing.getOrdersMoney() : BigDecimal.ZERO;
                BigDecimal itemOrdersMoney = item.getOrdersMoney() != null ? item.getOrdersMoney() : BigDecimal.ZERO;
                existing.setOrdersMoney(existingOrdersMoney.add(itemOrdersMoney));
                
                Integer existingOrders = existing.getOrders() != null ? existing.getOrders() : 0;
                Integer itemOrders = item.getOrders() != null ? item.getOrders() : 0;
                existing.setOrders(existingOrders + itemOrders);
                
                Integer existingViews = existing.getViews() != null ? existing.getViews() : 0;
                Integer itemViews = item.getViews() != null ? item.getViews() : 0;
                existing.setViews(existingViews + itemViews);
                
                Integer existingClicks = existing.getClicks() != null ? existing.getClicks() : 0;
                Integer itemClicks = item.getClicks() != null ? item.getClicks() : 0;
                existing.setClicks(existingClicks + itemClicks);
            } else {
                resultMap.put(key, item);
            }
        }
        
        log.info("查询门店维度广告费用完成，查询条件：clientIds={}, startDate={}, endDate={}, 结果数量={}", 
                clientIds, startDate, endDate, resultMap.size());
        
        return resultMap;
    }

    @Override
    public Map<String, OzonAdCampaignsItemDO> getAdCostByInProcessDateAndSkuGroupByDate(
            List<String> clientIds, 
            List<String> platformSkuIds, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        Map<String, OzonAdCampaignsItemDO> resultMap = new HashMap<>();
        
        // 使用简单SQL查询数据
        LambdaQueryWrapperX<OzonAdCampaignsItemDO> queryWrapper = new LambdaQueryWrapperX<OzonAdCampaignsItemDO>()
                .inIfPresent(OzonAdCampaignsItemDO::getClientId, clientIds)
                .betweenIfPresent(OzonAdCampaignsItemDO::getDate, startDate, endDate);

        List<OzonAdCampaignsItemDO> adItems = ozonAdCampaignsItemMapper.selectList(queryWrapper);

        // 在Java中按日期+门店+SKU分组聚合
        for (OzonAdCampaignsItemDO item : adItems) {
            // 防止空指针异常
            if (item.getDate() == null || item.getClientId() == null || item.getPlatformSkuId() == null) {
                log.warn("跳过无效数据：date={}, clientId={}, platformSkuId={}", item.getDate(), item.getClientId(), item.getPlatformSkuId());
                continue;
            }
            
            String key = item.getDate() + "_" + item.getClientId() + "_" + item.getPlatformSkuId();
            
            if (resultMap.containsKey(key)) {
                // 聚合数据
                OzonAdCampaignsItemDO existing = resultMap.get(key);
                // 防止BigDecimal为null的情况
                BigDecimal existingMoneySpent = existing.getMoneySpent() != null ? existing.getMoneySpent() : BigDecimal.ZERO;
                BigDecimal itemMoneySpent = item.getMoneySpent() != null ? item.getMoneySpent() : BigDecimal.ZERO;
                existing.setMoneySpent(existingMoneySpent.add(itemMoneySpent));
                
                BigDecimal existingOrdersMoney = existing.getOrdersMoney() != null ? existing.getOrdersMoney() : BigDecimal.ZERO;
                BigDecimal itemOrdersMoney = item.getOrdersMoney() != null ? item.getOrdersMoney() : BigDecimal.ZERO;
                existing.setOrdersMoney(existingOrdersMoney.add(itemOrdersMoney));
                
                Integer existingOrders = existing.getOrders() != null ? existing.getOrders() : 0;
                Integer itemOrders = item.getOrders() != null ? item.getOrders() : 0;
                existing.setOrders(existingOrders + itemOrders);
                
                Integer existingViews = existing.getViews() != null ? existing.getViews() : 0;
                Integer itemViews = item.getViews() != null ? item.getViews() : 0;
                existing.setViews(existingViews + itemViews);
                
                Integer existingClicks = existing.getClicks() != null ? existing.getClicks() : 0;
                Integer itemClicks = item.getClicks() != null ? item.getClicks() : 0;
                existing.setClicks(existingClicks + itemClicks);
            } else {
                resultMap.put(key, item);
            }
        }
        
        log.info("查询SKU维度广告费用(按日期分组)完成，查询条件：clientIds={}, platformSkuIds={}, startDate={}, endDate={}, 结果数量={}", 
                clientIds, platformSkuIds, startDate, endDate, resultMap.size());
        
        return resultMap;
    }

    @Override
    public Map<String, OzonAdCampaignsItemDO> getAdCostByInProcessDateForClientGroupByDate(
            List<String> clientIds, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        Map<String, OzonAdCampaignsItemDO> resultMap = new HashMap<>();

        // 使用简单SQL查询数据
        LambdaQueryWrapperX<OzonAdCampaignsItemDO> queryWrapper = new LambdaQueryWrapperX<OzonAdCampaignsItemDO>()
                .inIfPresent(OzonAdCampaignsItemDO::getClientId, clientIds)
                .betweenIfPresent(OzonAdCampaignsItemDO::getDate, startDate, endDate);
        
        List<OzonAdCampaignsItemDO> adItems = ozonAdCampaignsItemMapper.selectList(queryWrapper);
        
        // 在Java中按日期+门店分组聚合
        for (OzonAdCampaignsItemDO item : adItems) {
            // 防止空指针异常
            if (item.getDate() == null || item.getClientId() == null) {
                log.warn("跳过无效数据：date={}, clientId={}", item.getDate(), item.getClientId());
                continue;
            }
            
            String key = item.getDate() + "_" + item.getClientId();
            
            if (resultMap.containsKey(key)) {
                // 聚合数据
                OzonAdCampaignsItemDO existing = resultMap.get(key);
                // 防止BigDecimal为null的情况
                BigDecimal existingMoneySpent = existing.getMoneySpent() != null ? existing.getMoneySpent() : BigDecimal.ZERO;
                BigDecimal itemMoneySpent = item.getMoneySpent() != null ? item.getMoneySpent() : BigDecimal.ZERO;
                existing.setMoneySpent(existingMoneySpent.add(itemMoneySpent));
                
                BigDecimal existingOrdersMoney = existing.getOrdersMoney() != null ? existing.getOrdersMoney() : BigDecimal.ZERO;
                BigDecimal itemOrdersMoney = item.getOrdersMoney() != null ? item.getOrdersMoney() : BigDecimal.ZERO;
                existing.setOrdersMoney(existingOrdersMoney.add(itemOrdersMoney));
                
                Integer existingOrders = existing.getOrders() != null ? existing.getOrders() : 0;
                Integer itemOrders = item.getOrders() != null ? item.getOrders() : 0;
                existing.setOrders(existingOrders + itemOrders);
                
                Integer existingViews = existing.getViews() != null ? existing.getViews() : 0;
                Integer itemViews = item.getViews() != null ? item.getViews() : 0;
                existing.setViews(existingViews + itemViews);
                
                Integer existingClicks = existing.getClicks() != null ? existing.getClicks() : 0;
                Integer itemClicks = item.getClicks() != null ? item.getClicks() : 0;
                existing.setClicks(existingClicks + itemClicks);
            } else {
                resultMap.put(key, item);
            }
        }
        
        log.info("查询门店维度广告费用(按日期分组)完成，查询条件：clientIds={}, startDate={}, endDate={}, 结果数量={}", 
                clientIds, startDate, endDate, resultMap.size());
        
        return resultMap;
    }
}