package cn.iocoder.yudao.module.dm.service.ad;

import java.time.LocalDate;
import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

/**
 * 广告活动 Service 接口
 *
 * @author Zeno
 */
public interface OzonAdCampaignsService {

    /**
     * 创建广告活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createOzonAdCampaigns(@Valid OzonAdCampaignsSaveReqVO createReqVO);

    /**
     * 更新广告活动
     *
     * @param updateReqVO 更新信息
     */
    void updateOzonAdCampaigns(@Valid OzonAdCampaignsSaveReqVO updateReqVO);

    /**
     * 删除广告活动
     *
     * @param id 编号
     */
    void deleteOzonAdCampaigns(Integer id);

    /**
     * 获得广告活动
     *
     * @param id 编号
     * @return 广告活动
     */
    OzonAdCampaignsDO getOzonAdCampaigns(Integer id);

    /**
     * 获得广告活动
     * @param campaignId
     * @param date
     * @return
     */
    OzonAdCampaignsDO getOzonAdCampaignByCampaignId(String campaignId, LocalDate date);

    /**
     * 获得广告活动分页
     *
     * @param pageReqVO 分页查询
     * @return 广告活动分页
     */
    PageResult<OzonAdCampaignsDO> getOzonAdCampaignsPage(OzonAdCampaignsPageReqVO pageReqVO);

    /**
     * 统计分页
     *
     * @param page
     * @param reqVO
     * @return
     */
    IPage<OzonAdCampaignsPageRespVO> selectPage2(IPage<OzonAdCampaignsPageRespVO> page, OzonAdCampaignsPageReqVO reqVO);

    /**
     * 获得广告活动列表
     * @param clientId
     * @param date
     * @return
     */
    List<OzonAdCampaignsDO> getOzonAdCampaignsListByClientId(String clientId, LocalDate date);

    /**
     * 获得广告活动列表
     * @param clientIds
     * @param date
     * @return
     */
    List<OzonAdCampaignsDO> getOzonAdCampaignsListByClientIds(String[] clientIds, LocalDate date);
    // ==================== 子表（广告明细） ====================

    /**
     * 获得广告明细列表
     *
     * @param campaignId 活动ID
     * @return 广告明细列表
     */
    List<OzonAdCampaignsItemDO> getOzonAdCampaignsItemListByCampaignId(String campaignId, LocalDate date);

    /**
     * 批量获得广告明细列表
     *
     * @param platformSkuIds
     * @param begin
     * @param end
     * @return
     */
    List<OzonAdCampaignsItemDO> batchAdCampaignsItemList(List<String> platformSkuIds, LocalDate begin, LocalDate end);

    /**
     * 获得广告明细列表
     *
     * @param campaignId    活动ID
     * @param platformSkuId 平台SKU
     * @param date          日期
     * @return 广告明细列表
     */
    OzonAdCampaignsItemDO getOzonAdCampaignItemByCampaignId(String campaignId, String platformSkuId, LocalDate date);

    /**
     *
     * @param campaignId
     * @param platformSkuId
     * @param orderId
     * @param date
     * @return
     */
    OzonAdCampaignsItemDO getOzonAdCampaignItemByOrderId(String campaignId, String platformSkuId, String orderId, LocalDate date);

    /**
     * 创建广告明细
     * @param adCampaignsItemDOList
     */
    void createOzonAdCampaignsItem(List<OzonAdCampaignsItemDO> adCampaignsItemDOList);

    /**
     * 更新广告明细
     * @param adCampaignsItemDOList
     */
    void updateOzonAdCampaignsItem(List<OzonAdCampaignsItemDO> adCampaignsItemDOList);
}