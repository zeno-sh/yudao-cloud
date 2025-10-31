package cn.iocoder.yudao.module.dm.service.ozonshopmapping;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.dm.controller.admin.shop.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.dto.ClientSimpleInfoDTO;

/**
 * ozon店铺 Service 接口
 *
 * @author zeno
 */
public interface OzonShopMappingService {

    /**
     * 创建ozon店铺
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createOzonShopMapping(@Valid OzonShopMappingSaveReqVO createReqVO);

    /**
     * 更新ozon店铺
     *
     * @param updateReqVO 更新信息
     */
    void updateOzonShopMapping(@Valid OzonShopMappingSaveReqVO updateReqVO);

    /**
     * 删除ozon店铺
     *
     * @param id 编号
     */
    void deleteOzonShopMapping(Integer id);

    /**
     * 获得ozon店铺
     *
     * @param id 编号
     * @return ozon店铺
     */
    OzonShopMappingDO getOzonShopMapping(Integer id);

    /**
     * 根据授权查询可用门店
     *
     * @return
     */
    List<OzonShopMappingSimpleRespVO> getOzonShopMappingByAuth();

    /**
     * 获得ozon店铺
     *
     * @param clientId
     * @return
     */
    OzonShopMappingDO getOzonShopMappingByClientId(String clientId);

    /**
     * 获得ozon店铺分页
     *
     * @param pageReqVO 分页查询
     * @return ozon店铺分页
     */
    PageResult<OzonShopMappingDO> getOzonShopMappingPage(OzonShopMappingPageReqVO pageReqVO);

    /**
     * 获取所有门店列表
     *
     * @return
     */
    List<OzonShopMappingDO> getOzonShopList();

    /**
     * 根据ids查询门店列表
     *
     * @param ids
     * @return
     */
    List<OzonShopMappingDO> getShopListByIds(Collection<Long> ids);

    /**
     * 根据 clientIds 查询门店列表
     *
     * @param clientIds
     * @return
     */
    List<OzonShopMappingDO> batchShopListByClientIds(Collection<String> clientIds);

    /**
     * 批量查询门店信息
     *
     * @param clientIds
     * @return
     */
    Map<String, ClientSimpleInfoDTO> batchSimpleInfoByClientIds(List<String> clientIds);
}