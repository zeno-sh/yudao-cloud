package cn.iocoder.yudao.module.dm.service.online;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.mysql.ozonshopmapping.OzonShopMappingMapper;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OnlineProductStatusEnum;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.dm.controller.admin.online.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.online.OzonProductOnlineMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 在线商品 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class OzonProductOnlineServiceImpl implements OzonProductOnlineService {

    @Resource
    private OzonProductOnlineMapper ozonProductOnlineMapper;
    @Resource
    private OzonShopMappingMapper ozonShopMappingMapper;

    @Override
    public Long createOzonProductOnline(OzonProductOnlineSaveReqVO createReqVO) {
        // 插入
        OzonProductOnlineDO ozonProductOnline = BeanUtils.toBean(createReqVO, OzonProductOnlineDO.class);
        ozonProductOnlineMapper.insert(ozonProductOnline);
        // 返回
        return ozonProductOnline.getId();
    }

    @Override
    public void updateOzonProductOnline(OzonProductOnlineSaveReqVO updateReqVO) {
        // 校验存在
        validateOzonProductOnlineExists(updateReqVO.getId());
//        validateMappingExist(updateReqVO.getClientId(), updateReqVO.getDmProductId());

        // 更新
        OzonProductOnlineDO updateObj = BeanUtils.toBean(updateReqVO, OzonProductOnlineDO.class);
        ozonProductOnlineMapper.updateById(updateObj);
    }

    private void validateMappingExist(String clientId, Long dmProductId) {
        if (null == dmProductId) {
            return;
        }

        LambdaQueryWrapperX<OzonProductOnlineDO> query = new LambdaQueryWrapperX<OzonProductOnlineDO>()
                .eq(OzonProductOnlineDO::getClientId, clientId)
                .eq(OzonProductOnlineDO::getDmProductId, dmProductId)
                .eq(OzonProductOnlineDO::getIsArchived, false)
                .in(OzonProductOnlineDO::getStatus, Arrays.asList(OnlineProductStatusEnum.ONLINE.getCode(), OnlineProductStatusEnum.READY.getCode()));

        if (ozonProductOnlineMapper.selectCount(query) > 0) {
            throw exception(OZON_PRODUCT_ONLINE_MAPPING_EXISTS);
        }
    }

    @Override
    public void deleteOzonProductOnline(Long id) {
        // 校验存在
        validateOzonProductOnlineExists(id);
        // 删除
        ozonProductOnlineMapper.deleteById(id);
    }

    private void validateOzonProductOnlineExists(Long id) {
        if (ozonProductOnlineMapper.selectById(id) == null) {
            throw exception(OZON_PRODUCT_ONLINE_NOT_EXISTS);
        }
    }

    @Override
    public OzonProductOnlineDO getOzonProductOnline(Long id) {
        return ozonProductOnlineMapper.selectById(id);
    }

    @Override
    public String getPlatformUrl(String clientId, String offerId) {
        OzonProductOnlineDO ozonProductOnlineDO = ozonProductOnlineMapper.selectOne(OzonProductOnlineDO::getOfferId, offerId, OzonProductOnlineDO::getClientId, clientId);
        if (ozonProductOnlineDO == null) {
            return null;
        }
        OzonShopMappingDO ozonShopMappingDO = ozonShopMappingMapper.selectOne(OzonShopMappingDO::getClientId, clientId);
        if (ozonShopMappingDO.getPlatform().equals(DmPlatformEnum.OZON.getPlatformId()) || ozonShopMappingDO.getPlatform().equals(DmPlatformEnum.OZON_GLOBAL.getPlatformId())) {
            return "https://ozon.ru/product/" + ozonProductOnlineDO.getPlatformSkuId();
        } else if (ozonShopMappingDO.getPlatform().equals(DmPlatformEnum.WB_GLOBAL.getPlatformId())) {
            return String.format("https://www.wildberries.ru/catalog/%s/detail.aspx?targetUrl=GP", ozonProductOnlineDO.getProductId());
        } else if (ozonShopMappingDO.getPlatform().equals(DmPlatformEnum.WB.getPlatformId())) {
            return String.format("https://www.wildberries.ru/catalog/%s/detail.aspx", ozonProductOnlineDO.getProductId());
        }
        return null;
    }

    @Override
    public List<OzonProductOnlineDO> getProductOnlineByOfferId(String offerId) {
        return ozonProductOnlineMapper.selectList(new LambdaQueryWrapperX<OzonProductOnlineDO>()
                .likeIfPresent(OzonProductOnlineDO::getOfferId, offerId));
    }

    @Override
    public OzonProductOnlineDO getOzonProductOnlineByOfferId(String clientId, String offerId) {
        return ozonProductOnlineMapper.selectOne(OzonProductOnlineDO::getOfferId, offerId, OzonProductOnlineDO::getClientId, clientId);
    }

    @Override
    public OzonProductOnlineDO getOzonProductOnlineBySkuId(String clientId, String platformSkuId) {
        return ozonProductOnlineMapper.selectOne(OzonProductOnlineDO::getPlatformSkuId, platformSkuId, OzonProductOnlineDO::getClientId, clientId);
    }

    @Override
    public List<OzonProductOnlineDO> batchOzonProductOnlineByOfferId(Collection<String> offerIds) {
        return ozonProductOnlineMapper.selectList(OzonProductOnlineDO::getOfferId, offerIds);
    }

    @Override
    public List<OzonProductOnlineDO> batchOzonProductOnlineBySkuIds(Collection<String> platformSkuIds) {
        return ozonProductOnlineMapper.selectList(OzonProductOnlineDO::getPlatformSkuId, platformSkuIds);
    }

    @Override
    public List<OzonProductOnlineDO> getAllProductOnlineByClientId(String clientId) {
        return ozonProductOnlineMapper.selectList(OzonProductOnlineDO::getClientId, clientId);
    }

    @Override
    public List<OzonProductOnlineDO> getAllProductOnlineByClientIds(Collection<String> clientIds) {
        return ozonProductOnlineMapper.selectList(OzonProductOnlineDO::getClientId, clientIds);
    }


    @Override
    public Map<String, Map<Long, String>> batchProductMapping(Collection<String> clientIds, Collection<Long> productIds) {
        LambdaQueryWrapperX<OzonProductOnlineDO> wrapperX = new LambdaQueryWrapperX<OzonProductOnlineDO>()
                .inIfPresent(OzonProductOnlineDO::getClientId, clientIds)
                .inIfPresent(OzonProductOnlineDO::getDmProductId, productIds);

        List<OzonProductOnlineDO> productOnlineDOList = ozonProductOnlineMapper.selectList(wrapperX);

        // 如果查询结果为空，则返回空Map
        if (CollectionUtils.isEmpty(productOnlineDOList)) {
            return Maps.newHashMap();
        }

        // 创建结果Map：clientId -> (dmProductId -> offerId)
        Map<String, Map<Long, String>> resultMap = new HashMap<>();

        // 遍历查询结果，构造嵌套的Map结构
        for (OzonProductOnlineDO product : productOnlineDOList) {
            String clientId = product.getClientId();
            Long dmProductId = product.getDmProductId();
            String offerId = product.getOfferId();

            // 如果resultMap中还没有该clientId，先初始化一个空的内部Map
            resultMap.computeIfAbsent(clientId, k -> new HashMap<>());

            // 添加dmProductId -> offerId的映射
            resultMap.get(clientId).put(dmProductId, offerId);
        }

        return resultMap;
    }

    @Override
    public PageResult<OzonProductOnlineDO> getOzonProductOnlinePage(OzonProductOnlinePageReqVO pageReqVO) {
        return ozonProductOnlineMapper.selectPage(pageReqVO);
    }

    @Override
    public Map<String, Long> batchPlatformSkuMapping(Collection<String> platformSkuIds) {
        List<OzonProductOnlineDO> ozonProductOnlineDOList = ozonProductOnlineMapper.selectList(OzonProductOnlineDO::getPlatformSkuId, platformSkuIds);
        if (CollectionUtils.isEmpty(ozonProductOnlineDOList)) {
            return Collections.emptyMap();
        }

        return convertMap(ozonProductOnlineDOList.stream()
                .filter(product -> product.getPlatformSkuId() != null && product.getDmProductId() != null)
                .collect(Collectors.toList()),
                OzonProductOnlineDO::getPlatformSkuId,
                OzonProductOnlineDO::getDmProductId);
    }

    @Override
    public Map<String, Long> batchOfferIdDmProductMapping(Collection<Long> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return Collections.emptyMap();
        }

        List<OzonProductOnlineDO> ozonProductOnlineDOList = ozonProductOnlineMapper.selectList(OzonProductOnlineDO::getDmProductId, productIds);
        if (CollectionUtils.isEmpty(ozonProductOnlineDOList)) {
            return Collections.emptyMap();
        }

        // 返回 offerId -> dmProductId 的映射
        return ozonProductOnlineDOList.stream()
                .filter(product -> product.getOfferId() != null && product.getDmProductId() != null)
                .collect(Collectors.toMap(
                        OzonProductOnlineDO::getOfferId,
                        OzonProductOnlineDO::getDmProductId,
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public List<OzonProductOnlineDO> getOzonProductOnlinesByPlatformSkuId(String platformSkuId) {
        if (platformSkuId == null || platformSkuId.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 构建查询条件，查询所有匹配platformSkuId的商品
        LambdaQueryWrapperX<OzonProductOnlineDO> queryWrapper = new LambdaQueryWrapperX<OzonProductOnlineDO>()
                .eq(OzonProductOnlineDO::getPlatformSkuId, platformSkuId)
                .eq(OzonProductOnlineDO::getDeleted, false); // 只查询未删除的记录
                
        return ozonProductOnlineMapper.selectList(queryWrapper);
    }

    @Override
    public List<OzonProductOnlineDO> listOzonProductOnlinesByDmProductIds(Collection<Long> dmProductIds) {
        if (CollectionUtils.isEmpty(dmProductIds)) {
            return Collections.emptyList();
        }
        
        // 构建查询条件，批量查询指定dmProductId的商品
        LambdaQueryWrapperX<OzonProductOnlineDO> queryWrapper = new LambdaQueryWrapperX<OzonProductOnlineDO>()
                .in(OzonProductOnlineDO::getDmProductId, dmProductIds)
                .eq(OzonProductOnlineDO::getDeleted, false); // 只查询未删除的记录
                
        return ozonProductOnlineMapper.selectList(queryWrapper);
    }
}