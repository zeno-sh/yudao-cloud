package cn.iocoder.yudao.module.chrome.service.usage;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import cn.iocoder.yudao.module.chrome.controller.admin.usage.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.usage.UsageRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.chrome.dal.mysql.usage.UsageRecordMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * Chrome使用记录 Service 实现类
 *
 * @author Jax
 */
@Service
@Validated
public class UsageRecordServiceImpl implements UsageRecordService {

    @Resource
    private UsageRecordMapper usageRecordMapper;

    @Override
    public Long createUsageRecord(UsageRecordSaveReqVO createReqVO) {
        // 插入
        UsageRecordDO usageRecord = BeanUtils.toBean(createReqVO, UsageRecordDO.class);
        usageRecordMapper.insert(usageRecord);
        // 返回
        return usageRecord.getId();
    }

    @Override
    public void updateUsageRecord(UsageRecordSaveReqVO updateReqVO) {
        // 校验存在
        validateUsageRecordExists(updateReqVO.getId());
        // 更新
        UsageRecordDO updateObj = BeanUtils.toBean(updateReqVO, UsageRecordDO.class);
        usageRecordMapper.updateById(updateObj);
    }

    @Override
    public void deleteUsageRecord(Long id) {
        // 校验存在
        validateUsageRecordExists(id);
        // 删除
        usageRecordMapper.deleteById(id);
    }

    private void validateUsageRecordExists(Long id) {
        if (usageRecordMapper.selectById(id) == null) {
            throw exception(USAGE_RECORD_NOT_EXISTS);
        }
    }

    @Override
    public UsageRecordDO getUsageRecord(Long id) {
        return usageRecordMapper.selectById(id);
    }

    @Override
    public PageResult<UsageRecordDO> getUsageRecordPage(UsageRecordPageReqVO pageReqVO) {
        return usageRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public void recordUsage(Long userId, Integer featureType) {
        recordUsage(userId, featureType, 1, null);
    }
    
    @Override
    public void recordUsage(Long userId, Integer featureType, Integer creditsConsumed, String sellerProductId) {
        UsageRecordDO usageRecord = new UsageRecordDO();
        usageRecord.setUserId(userId);
        usageRecord.setFeatureType(featureType);
        usageRecord.setUsageCount(1);
        usageRecord.setCreditsConsumed(creditsConsumed != null ? creditsConsumed : 1);
        usageRecord.setSellerProductId(sellerProductId);
        LocalDateTime now = LocalDateTime.now();
        usageRecord.setUsageDate(now.toLocalDate());
        usageRecordMapper.insert(usageRecord);
    }

    @Override
    public int getTodayUsageCount(Long userId, Integer featureType) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        return usageRecordMapper.selectTodayUsageCount(userId, featureType, startOfDay, endOfDay);
    }

    @Override
    public int getMonthUsageCount(Long userId, Integer featureType) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = monthStart.atStartOfDay();
        LocalDateTime endOfMonth = monthStart.plusMonths(1).atStartOfDay();
        
        return usageRecordMapper.selectMonthUsageCount(userId, featureType, startOfMonth, endOfMonth);
    }

    @Override
    public int getTotalUsageCount(Long userId, Integer featureType) {
        return usageRecordMapper.selectTotalUsageCount(userId, featureType);
    }

}