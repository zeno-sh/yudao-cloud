package cn.iocoder.yudao.module.dm.dal.mysql.ad;

import java.time.LocalDate;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 广告明细 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface OzonAdCampaignsItemMapper extends BaseMapperX<OzonAdCampaignsItemDO> {

    default List<OzonAdCampaignsItemDO> selectListByCampaignId(String campaignId, LocalDate date) {
        return selectList(OzonAdCampaignsItemDO::getCampaignId, campaignId, OzonAdCampaignsItemDO::getDate, date);
    }

    default int deleteByCampaignId(String campaignId) {
        return delete(OzonAdCampaignsItemDO::getCampaignId, campaignId);
    }

}