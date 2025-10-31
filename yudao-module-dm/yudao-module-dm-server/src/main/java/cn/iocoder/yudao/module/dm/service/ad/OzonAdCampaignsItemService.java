package cn.iocoder.yudao.module.dm.service.ad;

import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 广告明细 Service 接口
 *
 * @author Zeno
 */
public interface OzonAdCampaignsItemService {

    /**
     * 根据接单日期范围和SKU查询广告费用
     *
     * @param clientIds 门店ID列表
     * @param platformSkuIds 平台SKU ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 广告费用数据，key为clientId_platformSkuId
     */
    Map<String, OzonAdCampaignsItemDO> getAdCostByInProcessDateAndSku(
            List<String> clientIds, 
            List<String> platformSkuIds, 
            LocalDate startDate, 
            LocalDate endDate);

    /**
     * 根据接单日期范围查询门店维度广告费用
     *
     * @param clientIds 门店ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 广告费用数据，key为clientId
     */
    Map<String, OzonAdCampaignsItemDO> getAdCostByInProcessDateForClient(
            List<String> clientIds, 
            LocalDate startDate, 
            LocalDate endDate);

    /**
     * 根据接单日期范围和SKU查询广告费用，按日期分组
     *
     * @param clientIds 门店ID列表
     * @param platformSkuIds 平台SKU ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 广告费用数据，key为date_clientId_platformSkuId
     */
    Map<String, OzonAdCampaignsItemDO> getAdCostByInProcessDateAndSkuGroupByDate(
            List<String> clientIds, 
            List<String> platformSkuIds, 
            LocalDate startDate, 
            LocalDate endDate);

    /**
     * 根据接单日期范围查询门店维度广告费用，按日期分组
     *
     * @param clientIds 门店ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 广告费用数据，key为date_clientId
     */
    Map<String, OzonAdCampaignsItemDO> getAdCostByInProcessDateForClientGroupByDate(
            List<String> clientIds, 
            LocalDate startDate, 
            LocalDate endDate);
} 