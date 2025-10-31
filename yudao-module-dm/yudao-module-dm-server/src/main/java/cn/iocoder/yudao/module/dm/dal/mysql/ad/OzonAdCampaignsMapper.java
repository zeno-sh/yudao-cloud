package cn.iocoder.yudao.module.dm.dal.mysql.ad;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 广告活动 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface OzonAdCampaignsMapper extends BaseMapperX<OzonAdCampaignsDO> {

    default PageResult<OzonAdCampaignsDO> selectPage(OzonAdCampaignsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OzonAdCampaignsDO>()
                .inIfPresent(OzonAdCampaignsDO::getClientId, Arrays.asList(reqVO.getClientIds()))
                .eqIfPresent(OzonAdCampaignsDO::getCampaignId, reqVO.getCampaignId())
                .eqIfPresent(OzonAdCampaignsDO::getTitle, reqVO.getTitle())
                .eqIfPresent(OzonAdCampaignsDO::getViews, reqVO.getViews())
                .eqIfPresent(OzonAdCampaignsDO::getClicks, reqVO.getClicks())
                .eqIfPresent(OzonAdCampaignsDO::getMoneySpent, reqVO.getMoneySpent())
                .eqIfPresent(OzonAdCampaignsDO::getAvgBid, reqVO.getAvgBid())
                .eqIfPresent(OzonAdCampaignsDO::getOrders, reqVO.getOrders())
                .eqIfPresent(OzonAdCampaignsDO::getOrdersMoney, reqVO.getOrdersMoney())
                .betweenIfPresent(OzonAdCampaignsDO::getDate, reqVO.getDate())
                .betweenIfPresent(OzonAdCampaignsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OzonAdCampaignsDO::getDate));
    }

    IPage<OzonAdCampaignsPageRespVO> selectPage2(IPage<OzonAdCampaignsPageRespVO> page,
                                                 @Param("reqVO") OzonAdCampaignsPageReqVO reqVO);
}