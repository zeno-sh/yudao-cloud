package cn.iocoder.yudao.module.dm.infrastructure.service;

/**
 * @author: Zeno
 * @createTime: 2024/08/27 22:15
 */
public interface PlatformWarehouseService<R,Q> {

    R queryWarehouse(Q warehouseQueryRequest);
}
