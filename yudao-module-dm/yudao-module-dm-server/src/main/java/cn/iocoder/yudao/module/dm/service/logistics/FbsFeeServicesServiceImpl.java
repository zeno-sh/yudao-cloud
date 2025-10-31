package cn.iocoder.yudao.module.dm.service.logistics;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.dm.controller.admin.logistics.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeServicesDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeDetailDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.logistics.FbsFeeServicesMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.logistics.FbsFeeDetailMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 收费项目 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class FbsFeeServicesServiceImpl implements FbsFeeServicesService {

    @Resource
    private FbsFeeServicesMapper fbsFeeServicesMapper;
    @Resource
    private FbsFeeDetailMapper fbsFeeDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFbsFeeServices(FbsFeeServicesSaveReqVO createReqVO) {
        // 插入
        FbsFeeServicesDO fbsFeeServices = BeanUtils.toBean(createReqVO, FbsFeeServicesDO.class);
        fbsFeeServicesMapper.insert(fbsFeeServices);

        // 插入子表
        createFbsFeeDetailList(fbsFeeServices.getId(), createReqVO.getFbsFeeDetails());
        // 返回
        return fbsFeeServices.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFbsFeeServices(FbsFeeServicesSaveReqVO updateReqVO) {
        // 校验存在
        validateFbsFeeServicesExists(updateReqVO.getId());
        // 更新
        FbsFeeServicesDO updateObj = BeanUtils.toBean(updateReqVO, FbsFeeServicesDO.class);
        fbsFeeServicesMapper.updateById(updateObj);

        // 更新子表
        updateFbsFeeDetailList(updateReqVO.getId(), updateReqVO.getFbsFeeDetails());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFbsFeeServices(Long id) {
        // 校验存在
        validateFbsFeeServicesExists(id);
        // 删除
        fbsFeeServicesMapper.deleteById(id);

        // 删除子表
        deleteFbsFeeDetailByServiceId(id);
    }

    private void validateFbsFeeServicesExists(Long id) {
        if (fbsFeeServicesMapper.selectById(id) == null) {
            throw exception(FBS_FEE_SERVICES_NOT_EXISTS);
        }
    }

    @Override
    public FbsFeeServicesDO getFbsFeeServices(Long id) {
        return fbsFeeServicesMapper.selectById(id);
    }

    @Override
    public PageResult<FbsFeeServicesDO> getFbsFeeServicesPage(FbsFeeServicesPageReqVO pageReqVO) {
        return fbsFeeServicesMapper.selectPage(pageReqVO);
    }

    @Override
    public List<FbsFeeServicesDO> getFbsFeeServicesByWarehouseId(Long warehouseId) {
        return fbsFeeServicesMapper.selectList(FbsFeeServicesDO::getWarehouseId, warehouseId);
    }

    @Override
    public Map<Long, List<FbsFeeServicesDO>> batchFbsFeeServices(Collection<Long> warehouseIds) {
        List<FbsFeeServicesDO> fbsFeeServicesDOS = fbsFeeServicesMapper.selectList(FbsFeeServicesDO::getWarehouseId, warehouseIds);
        if (CollectionUtils.isEmpty(fbsFeeServicesDOS)) {
            return Collections.emptyMap();
        }
        return convertMultiMap(fbsFeeServicesDOS, FbsFeeServicesDO::getWarehouseId);
    }

    // ==================== 子表（收费明细） ====================

    @Override
    public List<FbsFeeDetailDO> getFbsFeeDetailListByServiceId(Long serviceId) {
        return fbsFeeDetailMapper.selectListByServiceId(serviceId);
    }

    @Override
    public List<FbsFeeDetailDO> batchFbsFeeDetailListByServiceIds(Collection<Long> serviceIds) {
        return fbsFeeDetailMapper.selectList(FbsFeeDetailDO::getServiceId, serviceIds);
    }

    private void createFbsFeeDetailList(Long serviceId, List<FbsFeeDetailDO> list) {
        list.forEach(o -> o.setServiceId(serviceId));
        fbsFeeDetailMapper.insertBatch(list);
    }

    private void updateFbsFeeDetailList(Long serviceId, List<FbsFeeDetailDO> list) {
        deleteFbsFeeDetailByServiceId(serviceId);
		list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createFbsFeeDetailList(serviceId, list);
    }

    private void deleteFbsFeeDetailByServiceId(Long serviceId) {
        fbsFeeDetailMapper.deleteByServiceId(serviceId);
    }

}