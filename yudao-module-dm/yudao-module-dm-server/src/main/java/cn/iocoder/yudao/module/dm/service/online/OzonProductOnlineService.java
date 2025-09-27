package cn.iocoder.yudao.module.dm.service.online;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.online.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 在线商品 Service 接口
 *
 * @author Zeno
 */
public interface OzonProductOnlineService {

    /**
     * 创建在线商品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOzonProductOnline(@Valid OzonProductOnlineSaveReqVO createReqVO);

    /**
     * 更新在线商品
     *
     * @param updateReqVO 更新信息
     */
    void updateOzonProductOnline(@Valid OzonProductOnlineSaveReqVO updateReqVO);

    /**
     * 删除在线商品
     *
     * @param id 编号
     */
    void deleteOzonProductOnline(Long id);

    /**
     * 获得在线商品
     *
     * @param id 编号
     * @return 在线商品
     */
    OzonProductOnlineDO getOzonProductOnline(Long id);

    /**
     * 获得在线商品 url
     *
     * @param clientId
     * @param offerId
     * @return
     */
    String getPlatformUrl(String clientId, String offerId);

    /**
     * 获得在线商品
     *
     * @param offerId
     * @return
     */
    List<OzonProductOnlineDO> getProductOnlineByOfferId(String offerId);

    /**
     * 获得在线商品
     *
     * @param offerId 货号
     * @return 在线商品
     */
    OzonProductOnlineDO getOzonProductOnlineByOfferId(String clientId, String offerId);

    /**
     * 获得在线商品
     *
     * @param clientId
     * @param platformSkuId
     * @return
     */
    OzonProductOnlineDO getOzonProductOnlineBySkuId(String clientId, String platformSkuId);

    /**
     * 批量查询在线商品
     * @param offerIds
     * @return
     */
    List<OzonProductOnlineDO> batchOzonProductOnlineByOfferId(Collection<String> offerIds);

    /**
     * 批量查询在线商品
     * @param platformSkuIds
     * @return
     */
    List<OzonProductOnlineDO> batchOzonProductOnlineBySkuIds(Collection<String> platformSkuIds);

    /**
     * 获得全部在线商品
     * @param clientId
     * @return
     */
    List<OzonProductOnlineDO> getAllProductOnlineByClientId(String clientId);

    /**
     * 根据门店批量获取全部在线商品
     *
     * @param clientIds
     * @return
     */
    List<OzonProductOnlineDO> getAllProductOnlineByClientIds(Collection<String> clientIds);

    /**
     * 根据门店批量获取产品映射
     *
     * @param clientIds
     * @param productIds
     * @return
     */
    Map<String, Map<Long, String>> batchProductMapping(Collection<String> clientIds, Collection<Long> productIds);

    /**
     * 获得在线商品分页
     *
     * @param pageReqVO 分页查询
     * @return 在线商品分页
     */
    PageResult<OzonProductOnlineDO> getOzonProductOnlinePage(OzonProductOnlinePageReqVO pageReqVO);

    /**
     * 批量获取平台SKU映射
     *
     * @param platformSkuIds
     * @return
     */
    Map<String, Long> batchPlatformSkuMapping(Collection<String> platformSkuIds);

    /**
     * 批量获取DM产品映射
     *
     * @param productIds
     * @return
     */
    Map<String, Long> batchOfferIdDmProductMapping(Collection<Long> productIds);

    /**
     * 根据平台SKU ID获取所有匹配的在线商品
     *
     * @param platformSkuId 平台SKU ID
     * @return 商品列表
     */
    List<OzonProductOnlineDO> getOzonProductOnlinesByPlatformSkuId(String platformSkuId);

    /**
     * 根据dmProductIds批量查询在线商品
     *
     * @param dmProductIds 本地产品ID集合
     * @return 商品列表
     */
    List<OzonProductOnlineDO> listOzonProductOnlinesByDmProductIds(Collection<Long> dmProductIds);
}