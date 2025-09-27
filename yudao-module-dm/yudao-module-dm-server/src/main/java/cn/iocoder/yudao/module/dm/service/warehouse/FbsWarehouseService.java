package cn.iocoder.yudao.module.dm.service.warehouse;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseAuthDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseMappingDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 海外仓仓库 Service 接口
 *
 * @author Zeno
 */
public interface FbsWarehouseService {

    /**
     * 创建海外仓仓库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFbsWarehouse(@Valid FbsWarehouseSaveReqVO createReqVO);

    /**
     * 更新海外仓仓库
     *
     * @param updateReqVO 更新信息
     */
    void updateFbsWarehouse(@Valid FbsWarehouseSaveReqVO updateReqVO);

    /**
     * 删除海外仓仓库
     *
     * @param id 编号
     */
    void deleteFbsWarehouse(Long id);

    /**
     * 获得海外仓仓库
     *
     * @param id 编号
     * @return 海外仓仓库
     */
    FbsWarehouseDO getFbsWarehouse(Long id);

    /**
     * 获得海外仓仓库分页
     *
     * @param pageReqVO 分页查询
     * @return 海外仓仓库分页
     */
    PageResult<FbsWarehouseDO> getFbsWarehousePage(FbsWarehousePageReqVO pageReqVO);

    /**
     * 批量获取海外仓仓库
     *
     * @param ids
     * @return
     */
    List<FbsWarehouseDO> batchFbsWarehouse(Collection<Long> ids);

    /**
     * 获得所有海外仓仓库
     *
     * @return 海外仓仓库列表
     */
    List<FbsWarehouseDO> getAllFbsWarehouse();

    // ==================== 子表（海外仓授权信息） ====================

    /**
     * 获得海外仓授权信息
     *
     * @param warehouseId 关联的仓库ID
     * @return 海外仓授权信息
     */
    FbsWarehouseAuthDO getFbsWarehouseAuthByWarehouseId(Long warehouseId);

    /**
     * 获取所有海外仓授权信息
     *
     * @return
     */
    List<FbsWarehouseAuthDO> getAllFbsWarehouseAuthList();

    /**
     * 批量获取海外仓授权信息
     *
     * @param warehouseIds
     * @return
     */
    Map<Long, FbsWarehouseAuthDO> getFbsWarehouseAuthMapByWarehouseIds(Collection<Long> warehouseIds);
    // ==================== 子表（海外仓平台仓映射） ====================

    /**
     * 获得海外仓平台仓映射列表
     *
     * @param warehouseId 关联的仓库ID
     * @return 海外仓平台仓映射列表
     */
    List<FbsWarehouseMappingDO> getFbsWarehouseMappingListByWarehouseId(Long warehouseId);

    /**
     * 获取海外仓平台仓映射
     *
     * @param clientIds
     * @return
     */
    Map<Long, FbsWarehouseMappingDO> getFbsWarehouseMappingByClientIds(Collection<String> clientIds);

    /**
     * 获取海外仓平台仓映射
     *
     * @param platformWarehouseId
     * @return
     */
    FbsWarehouseMappingDO getFbsWarehouseMappingByPlatformWarehouseId(String platformWarehouseId);

    /**
     * 批量获取海外仓平台仓映射
     *
     * @param platformWarehouseIds
     * @return
     */
    Map<String, FbsWarehouseMappingDO> getFbsWarehouseMappingByPlatformWarehouseIds(Collection<String> platformWarehouseIds);
}