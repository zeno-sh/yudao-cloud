package cn.iocoder.yudao.module.dm.api;

import cn.iocoder.yudao.module.dm.dto.ShopMappingDTO;

import java.util.List;

/**
 * 门店映射查询服务
 * 
 * @author Jax
 * @createTime: 2025/01/16 10:00
 */
public interface DmShopMappingQueryService {

    /**
     * 根据客户端ID查询绑定门店信息
     * 
     * @param clientId 客户端ID
     * @return 门店映射信息
     */
    ShopMappingDTO getShopMappingByClientId(String clientId);

    /**
     * 根据客户端ID列表批量查询绑定门店信息
     * 
     * @param clientIds 客户端ID列表
     * @return 门店映射信息列表
     */
    List<ShopMappingDTO> batchGetShopMappingByClientIds(List<String> clientIds);

    /**
     * 查询所有可用的门店映射信息
     * 
     * @return 门店映射信息列表
     */
    List<ShopMappingDTO> getAllAvailableShopMappings();
} 