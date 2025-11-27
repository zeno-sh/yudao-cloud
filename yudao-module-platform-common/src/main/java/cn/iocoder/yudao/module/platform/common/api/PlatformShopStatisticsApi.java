package cn.iocoder.yudao.module.platform.common.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.platform.common.dto.ShopStatisticsDTO;
import cn.iocoder.yudao.module.platform.common.dto.ShopStatisticsQueryDTO;

import java.util.List;

/**
 * 平台店铺统计数据 API 接口规范（纯接口定义）
 * <p>
 * 各电商平台（Amazon、Coupang、Ozon等）的 API 模块需要：
 * 1. 继承此接口
 * 2. 添加 @FeignClient 注解指定服务名
 * 3. 定义各自的 PREFIX 路径前缀
 * 4. 为方法添加 @GetMapping/@PostMapping 等注解
 * </p>
 * <p>
 * 确保返回统一的 {@link ShopStatisticsDTO} 数据结构
 * </p>
 *
 * @author Jax
 */
public interface PlatformShopStatisticsApi {

    /**
     * 批量查询店铺统计数据
     *
     * @param queryDTO 查询条件
     * @return 店铺统计数据列表
     */
    CommonResult<List<ShopStatisticsDTO>> getShopStatistics(ShopStatisticsQueryDTO queryDTO);

}
