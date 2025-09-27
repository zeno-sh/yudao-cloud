package cn.iocoder.yudao.module.dm.service.warehouse;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseAuthDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseMappingDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.dm.dal.mysql.warehouse.FbsWarehouseMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.warehouse.FbsWarehouseAuthMapper;
import cn.iocoder.yudao.module.dm.dal.mysql.warehouse.FbsWarehouseMappingMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.*;

/**
 * 海外仓仓库 Service 实现类
 *
 * @author Zeno
 */
@Service
@Validated
public class FbsWarehouseServiceImpl implements FbsWarehouseService {

    @Resource
    private FbsWarehouseMapper fbsWarehouseMapper;
    @Resource
    private FbsWarehouseAuthMapper fbsWarehouseAuthMapper;
    @Resource
    private FbsWarehouseMappingMapper fbsWarehouseMappingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFbsWarehouse(FbsWarehouseSaveReqVO createReqVO) {
        // 插入
        FbsWarehouseDO fbsWarehouse = BeanUtils.toBean(createReqVO, FbsWarehouseDO.class);
        fbsWarehouseMapper.insert(fbsWarehouse);

        // 插入子表
        createFbsWarehouseAuth(fbsWarehouse.getId(), createReqVO.getFbsWarehouseAuth());
        createFbsWarehouseMappingList(fbsWarehouse.getId(), createReqVO.getFbsWarehouseMappings());
        // 返回
        return fbsWarehouse.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFbsWarehouse(FbsWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        validateFbsWarehouseExists(updateReqVO.getId());
        // 更新
        FbsWarehouseDO updateObj = BeanUtils.toBean(updateReqVO, FbsWarehouseDO.class);
        fbsWarehouseMapper.updateById(updateObj);

        // 更新子表
        updateFbsWarehouseAuth(updateReqVO.getId(), updateReqVO.getFbsWarehouseAuth());
        updateFbsWarehouseMappingList(updateReqVO.getId(), updateReqVO.getFbsWarehouseMappings());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFbsWarehouse(Long id) {
        // 校验存在
        validateFbsWarehouseExists(id);
        // 删除
        fbsWarehouseMapper.deleteById(id);

        // 删除子表
        deleteFbsWarehouseAuthByWarehouseId(id);
        deleteFbsWarehouseMappingByWarehouseId(id);
    }

    private void validateFbsWarehouseExists(Long id) {
        if (fbsWarehouseMapper.selectById(id) == null) {
            throw exception(FBS_WAREHOUSE_NOT_EXISTS);
        }
    }

    @Override
    public FbsWarehouseDO getFbsWarehouse(Long id) {
        return fbsWarehouseMapper.selectById(id);
    }

    @Override
    public PageResult<FbsWarehouseDO> getFbsWarehousePage(FbsWarehousePageReqVO pageReqVO) {
        return fbsWarehouseMapper.selectPage(pageReqVO);
    }

    @Override
    public List<FbsWarehouseDO> batchFbsWarehouse(Collection<Long> ids) {
        return fbsWarehouseMapper.selectList(FbsWarehouseDO::getId, ids);
    }

    @Override
    public List<FbsWarehouseDO> getAllFbsWarehouse() {
        return fbsWarehouseMapper.selectList();
    }


    // ==================== 子表（海外仓授权信息） ====================

    @Override
    public FbsWarehouseAuthDO getFbsWarehouseAuthByWarehouseId(Long warehouseId) {
        return fbsWarehouseAuthMapper.selectByWarehouseId(warehouseId);
    }

    @Override
    public List<FbsWarehouseAuthDO> getAllFbsWarehouseAuthList() {
        return fbsWarehouseAuthMapper.selectList();
    }

    @Override
    public Map<Long, FbsWarehouseAuthDO> getFbsWarehouseAuthMapByWarehouseIds(Collection<Long> warehouseIds) {
        List<FbsWarehouseAuthDO> fbsWarehouseAuthDOList = fbsWarehouseAuthMapper.selectList(new LambdaQueryWrapperX<FbsWarehouseAuthDO>()
                .in(FbsWarehouseAuthDO::getWarehouseId, warehouseIds));
        return convertMap(fbsWarehouseAuthDOList, FbsWarehouseAuthDO::getWarehouseId);
    }

    private void createFbsWarehouseAuth(Long warehouseId, FbsWarehouseAuthDO fbsWarehouseAuth) {
        if (fbsWarehouseAuth == null) {
            return;
        }
        fbsWarehouseAuth.setWarehouseId(warehouseId);
        fbsWarehouseAuthMapper.insert(fbsWarehouseAuth);
    }

    private void updateFbsWarehouseAuth(Long warehouseId, FbsWarehouseAuthDO fbsWarehouseAuth) {
        if (fbsWarehouseAuth == null) {
            return;
        }
        fbsWarehouseAuth.setWarehouseId(warehouseId);
        fbsWarehouseAuth.setUpdater(null).setUpdateTime(null); // 解决更新情况下：updateTime 不更新
        fbsWarehouseAuthMapper.insertOrUpdate(fbsWarehouseAuth);
    }

    private void deleteFbsWarehouseAuthByWarehouseId(Long warehouseId) {
        fbsWarehouseAuthMapper.deleteByWarehouseId(warehouseId);
    }

    // ==================== 子表（海外仓平台仓映射） ====================

    @Override
    public List<FbsWarehouseMappingDO> getFbsWarehouseMappingListByWarehouseId(Long warehouseId) {
        return fbsWarehouseMappingMapper.selectListByWarehouseId(warehouseId);
    }

    @Override
    public Map<Long, FbsWarehouseMappingDO> getFbsWarehouseMappingByClientIds(Collection<String> clientIds) {
        List<FbsWarehouseMappingDO> fbsWarehouseMappingDOList = fbsWarehouseMappingMapper.selectList(new LambdaQueryWrapperX<FbsWarehouseMappingDO>()
                .inIfPresent(FbsWarehouseMappingDO::getClientId, clientIds));
        return convertMap(fbsWarehouseMappingDOList, FbsWarehouseMappingDO::getId);
    }

    @Override
    public FbsWarehouseMappingDO getFbsWarehouseMappingByPlatformWarehouseId(String platformWarehouseId) {
        return fbsWarehouseMappingMapper.selectOne(FbsWarehouseMappingDO::getPlatformWarehouseId, platformWarehouseId);
    }

    @Override
    public Map<String, FbsWarehouseMappingDO> getFbsWarehouseMappingByPlatformWarehouseIds(Collection<String> platformWarehouseIds) {
        List<FbsWarehouseMappingDO> fbsWarehouseMappingDOList = fbsWarehouseMappingMapper.selectList(new LambdaQueryWrapperX<FbsWarehouseMappingDO>()
                .inIfPresent(FbsWarehouseMappingDO::getPlatformWarehouseId, platformWarehouseIds));
        if (CollectionUtils.isEmpty(fbsWarehouseMappingDOList)) {
            return Collections.emptyMap();
        }
        return convertMap(fbsWarehouseMappingDOList, FbsWarehouseMappingDO::getPlatformWarehouseId);
    }

    private void createFbsWarehouseMappingList(Long warehouseId, List<FbsWarehouseMappingDO> list) {
        list.forEach(o -> o.setWarehouseId(warehouseId));
        fbsWarehouseMappingMapper.insertBatch(list);
    }

    private void updateFbsWarehouseMappingList(Long warehouseId, List<FbsWarehouseMappingDO> list) {
        deleteFbsWarehouseMappingByWarehouseId(warehouseId);
        list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createFbsWarehouseMappingList(warehouseId, list);
    }

    private void deleteFbsWarehouseMappingByWarehouseId(Long warehouseId) {
        fbsWarehouseMappingMapper.deleteByWarehouseId(warehouseId);
    }

}