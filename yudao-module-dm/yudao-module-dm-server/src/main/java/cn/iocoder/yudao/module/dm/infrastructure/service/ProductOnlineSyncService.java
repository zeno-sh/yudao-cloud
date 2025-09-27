package cn.iocoder.yudao.module.dm.infrastructure.service;

import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;

/**
 * @author: Zeno
 * @createTime: 2024/07/13 18:34
 */
public interface ProductOnlineSyncService {

    void sync(OzonShopMappingDO shopMappingDO);
}
