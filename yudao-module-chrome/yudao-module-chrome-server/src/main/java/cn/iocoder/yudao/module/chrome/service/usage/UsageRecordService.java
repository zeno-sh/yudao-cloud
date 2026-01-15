package cn.iocoder.yudao.module.chrome.service.usage;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.chrome.controller.admin.usage.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.usage.UsageRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * Chrome使用记录 Service 接口
 *
 * @author Jax
 */
public interface UsageRecordService {

    /**
     * 创建Chrome使用记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUsageRecord(@Valid UsageRecordSaveReqVO createReqVO);

    /**
     * 更新Chrome使用记录
     *
     * @param updateReqVO 更新信息
     */
    void updateUsageRecord(@Valid UsageRecordSaveReqVO updateReqVO);

    /**
     * 删除Chrome使用记录
     *
     * @param id 编号
     */
    void deleteUsageRecord(Long id);

    /**
     * 获得Chrome使用记录
     *
     * @param id 编号
     * @return Chrome使用记录
     */
    UsageRecordDO getUsageRecord(Long id);

    /**
     * 获得Chrome使用记录分页
     *
     * @param pageReqVO 分页查询
     * @return Chrome使用记录分页
     */
    PageResult<UsageRecordDO> getUsageRecordPage(UsageRecordPageReqVO pageReqVO);

    /**
     * 记录使用次数
     *
     * @param userId 用户ID
     * @param featureType 功能类型
     */
    void recordUsage(Long userId, Integer featureType);

    /**
     * 记录使用次数（完整版本）
     *
     * @param userId 用户ID
     * @param featureType 功能类型
     * @param creditsConsumed 消费积分数
     * @param sellerProductId 卖家商品ID
     */
    void recordUsage(Long userId, Integer featureType, Integer creditsConsumed, String sellerProductId);

    /**
     * 获取今日使用次数
     *
     * @param userId 用户ID
     * @param featureType 功能类型，为null时查询所有功能类型
     * @return 今日使用次数
     */
    int getTodayUsageCount(Long userId, Integer featureType);

    /**
     * 获取本月使用次数
     *
     * @param userId 用户ID
     * @param featureType 功能类型，为null时查询所有功能类型
     * @return 本月使用次数
     */
    int getMonthUsageCount(Long userId, Integer featureType);

    /**
     * 获取总使用次数
     *
     * @param userId 用户ID
     * @param featureType 功能类型，为null时查询所有功能类型
     * @return 总使用次数
     */
    int getTotalUsageCount(Long userId, Integer featureType);

}