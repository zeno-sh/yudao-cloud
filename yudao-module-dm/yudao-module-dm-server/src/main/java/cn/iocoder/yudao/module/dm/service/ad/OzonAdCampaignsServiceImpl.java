package cn.iocoder.yudao.module.dm.service.ad;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import cn.iocoder.yudao.module.dm.controller.admin.ad.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ad.OzonAdCampaignsItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.ad.OzonAdCampaignsMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.ad.OzonAdCampaignsItemMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 广告活动 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class OzonAdCampaignsServiceImpl implements OzonAdCampaignsService {

    @Resource
    private OzonAdCampaignsMapper ozonAdCampaignsMapper;
    @Resource
    private OzonAdCampaignsItemMapper ozonAdCampaignsItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createOzonAdCampaigns(OzonAdCampaignsSaveReqVO createReqVO) {
        // 插入
        OzonAdCampaignsDO ozonAdCampaigns = BeanUtils.toBean(createReqVO, OzonAdCampaignsDO.class);
        ozonAdCampaignsMapper.insert(ozonAdCampaigns);

        // 插入子表
        if (CollectionUtils.isNotEmpty(createReqVO.getOzonAdCampaignsItems())) {
            createOzonAdCampaignsItemList(ozonAdCampaigns.getCampaignId(), createReqVO.getOzonAdCampaignsItems());
        }
        // 返回
        return ozonAdCampaigns.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOzonAdCampaigns(OzonAdCampaignsSaveReqVO updateReqVO) {
        // 校验存在
        validateOzonAdCampaignsExists(updateReqVO.getId());
        // 更新
        OzonAdCampaignsDO updateObj = BeanUtils.toBean(updateReqVO, OzonAdCampaignsDO.class);
        ozonAdCampaignsMapper.updateById(updateObj);

        // 更新子表
        if (CollectionUtils.isNotEmpty(updateReqVO.getOzonAdCampaignsItems())) {
            updateOzonAdCampaignsItemList(updateReqVO.getCampaignId(), updateReqVO.getOzonAdCampaignsItems());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOzonAdCampaigns(Integer id) {
        // 校验存在
        validateOzonAdCampaignsExists(id);
        // 删除
        ozonAdCampaignsMapper.deleteById(id);

        // 删除子表
        OzonAdCampaignsDO ozonAdCampaignsDO = ozonAdCampaignsMapper.selectById(id);
        deleteOzonAdCampaignsItemByCampaignId(ozonAdCampaignsDO.getCampaignId());
    }

    private void validateOzonAdCampaignsExists(Integer id) {
        if (ozonAdCampaignsMapper.selectById(id) == null) {
            throw exception(OZON_AD_CAMPAIGNS_NOT_EXISTS);
        }
    }

    @Override
    public OzonAdCampaignsDO getOzonAdCampaigns(Integer id) {
        return ozonAdCampaignsMapper.selectById(id);
    }

    @Override
    public OzonAdCampaignsDO getOzonAdCampaignByCampaignId(String campaignId, LocalDate date) {

        return ozonAdCampaignsMapper.selectOne(new LambdaQueryWrapperX<OzonAdCampaignsDO>()
                .eqIfPresent(OzonAdCampaignsDO::getCampaignId, campaignId)
                .eqIfPresent(OzonAdCampaignsDO::getDate, date)
        );
    }

    @Override
    public PageResult<OzonAdCampaignsDO> getOzonAdCampaignsPage(OzonAdCampaignsPageReqVO pageReqVO) {
        return ozonAdCampaignsMapper.selectPage(pageReqVO);
    }

    @Override
    public IPage<OzonAdCampaignsPageRespVO> selectPage2(IPage<OzonAdCampaignsPageRespVO> page, OzonAdCampaignsPageReqVO reqVO) {
        return ozonAdCampaignsMapper.selectPage2(page, reqVO);
    }

    @Override
    public List<OzonAdCampaignsDO> getOzonAdCampaignsListByClientId(String clientId, LocalDate date) {
        return ozonAdCampaignsMapper.selectList(new LambdaQueryWrapperX<OzonAdCampaignsDO>()
                .eqIfPresent(OzonAdCampaignsDO::getClientId, clientId)
                .eqIfPresent(OzonAdCampaignsDO::getDate, date)
        );
    }

    @Override
    public List<OzonAdCampaignsDO> getOzonAdCampaignsListByClientIds(String[] clientIds, LocalDate date) {
        return ozonAdCampaignsMapper.selectList(new LambdaQueryWrapperX<OzonAdCampaignsDO>()
                .inIfPresent(OzonAdCampaignsDO::getClientId, clientIds)
                .eqIfPresent(OzonAdCampaignsDO::getDate, date)
        );
    }

    // ==================== 子表（广告明细） ====================

    @Override
    public List<OzonAdCampaignsItemDO> getOzonAdCampaignsItemListByCampaignId(String campaignId, LocalDate date) {
        return ozonAdCampaignsItemMapper.selectListByCampaignId(campaignId, date);
    }

    @Override
    public List<OzonAdCampaignsItemDO> batchAdCampaignsItemList(List<String> platformSkuIds, LocalDate begin, LocalDate end) {
        LambdaQueryWrapperX<OzonAdCampaignsItemDO> queryWrapperX = new LambdaQueryWrapperX<OzonAdCampaignsItemDO>()
                .inIfPresent(OzonAdCampaignsItemDO::getPlatformSkuId, platformSkuIds)
                .betweenIfPresent(OzonAdCampaignsItemDO::getDate, begin, end);
        return ozonAdCampaignsItemMapper.selectList(queryWrapperX);
    }

    @Override
    public OzonAdCampaignsItemDO getOzonAdCampaignItemByCampaignId(String campaignId, String platformSkuId, LocalDate date) {
        return ozonAdCampaignsItemMapper.selectOne(new LambdaQueryWrapperX<OzonAdCampaignsItemDO>()
                .eqIfPresent(OzonAdCampaignsItemDO::getCampaignId, campaignId)
                .eqIfPresent(OzonAdCampaignsItemDO::getPlatformSkuId, platformSkuId)
                .eqIfPresent(OzonAdCampaignsItemDO::getDate, date)
        );
    }

    @Override
    public OzonAdCampaignsItemDO getOzonAdCampaignItemByOrderId(String campaignId, String platformSkuId, String orderId, LocalDate date) {
        return ozonAdCampaignsItemMapper.selectOne(new LambdaQueryWrapperX<OzonAdCampaignsItemDO>()
                .eqIfPresent(OzonAdCampaignsItemDO::getCampaignId, campaignId)
                .eqIfPresent(OzonAdCampaignsItemDO::getPlatformSkuId, platformSkuId)
                .eqIfPresent(OzonAdCampaignsItemDO::getOrderId, orderId)
                .eqIfPresent(OzonAdCampaignsItemDO::getDate, date)
        );
    }

    @Override
    public void createOzonAdCampaignsItem(List<OzonAdCampaignsItemDO> adCampaignsItemDOList) {
        ozonAdCampaignsItemMapper.insertBatch(adCampaignsItemDOList);
    }

    @Override
    public void updateOzonAdCampaignsItem(List<OzonAdCampaignsItemDO> adCampaignsItemDOList) {
        ozonAdCampaignsItemMapper.updateBatch(adCampaignsItemDOList);
    }

    private void createOzonAdCampaignsItemList(String campaignId, List<OzonAdCampaignsItemDO> list) {
        list.forEach(o -> o.setCampaignId(campaignId));
        ozonAdCampaignsItemMapper.insertBatch(list);
    }

    private void updateOzonAdCampaignsItemList(String campaignId, List<OzonAdCampaignsItemDO> list) {
        deleteOzonAdCampaignsItemByCampaignId(campaignId);
        list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createOzonAdCampaignsItemList(campaignId, list);
    }

    private void deleteOzonAdCampaignsItemByCampaignId(String campaignId) {
        ozonAdCampaignsItemMapper.deleteByCampaignId(campaignId);
    }

}