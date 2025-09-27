package cn.iocoder.yudao.module.dm.api;

import java.util.Set;

/**
 * @author: Jax
 * @createTime: 2025/08/11 17:12
 */
public interface DmProductQueryService {

    /**
     * 获取用户产品权限的 ID 集合
     *
     * @return
     */
    Set<Long> getProductIdsByUserPermission();
}
