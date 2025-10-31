package cn.iocoder.yudao.module.dm.service.logistics;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.dm.controller.admin.logistics.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeServicesDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeDetailDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 收费项目 Service 接口
 *
 * @author Zeno
 */
public interface FbsFeeServicesService {

    /**
     * 创建收费项目
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFbsFeeServices(@Valid FbsFeeServicesSaveReqVO createReqVO);

    /**
     * 更新收费项目
     *
     * @param updateReqVO 更新信息
     */
    void updateFbsFeeServices(@Valid FbsFeeServicesSaveReqVO updateReqVO);

    /**
     * 删除收费项目
     *
     * @param id 编号
     */
    void deleteFbsFeeServices(Long id);

    /**
     * 获得收费项目
     *
     * @param id 编号
     * @return 收费项目
     */
    FbsFeeServicesDO getFbsFeeServices(Long id);

    /**
     * 获得收费项目分页
     *
     * @param pageReqVO 分页查询
     * @return 收费项目分页
     */
    PageResult<FbsFeeServicesDO> getFbsFeeServicesPage(FbsFeeServicesPageReqVO pageReqVO);

    /**
     * 获得收费项目
     *
     * @param warehouseId
     * @return
     */
    List<FbsFeeServicesDO> getFbsFeeServicesByWarehouseId(Long warehouseId);

    /**
     * 批量获得收费项目
     *
     * @param warehouseIds
     * @return
     */
    Map<Long, List<FbsFeeServicesDO>> batchFbsFeeServices(Collection<Long> warehouseIds);

    // ==================== 子表（收费明细） ====================

    /**
     * 获得收费明细列表
     *
     * @param serviceId 收费项目ID
     * @return 收费明细列表
     */
    List<FbsFeeDetailDO> getFbsFeeDetailListByServiceId(Long serviceId);

    /**
     * 批量获得收费明细列表
     *
     * @param serviceIds
     * @return
     */
    List<FbsFeeDetailDO> batchFbsFeeDetailListByServiceIds(Collection<Long> serviceIds);
}