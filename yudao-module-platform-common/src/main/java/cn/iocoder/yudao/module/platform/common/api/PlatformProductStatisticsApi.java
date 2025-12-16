package cn.iocoder.yudao.module.platform.common.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.platform.common.dto.ProductStatisticsDTO;
import cn.iocoder.yudao.module.platform.common.dto.ProductStatisticsQueryDTO;

/**
 * 平台产品统计数据 API 接口规范（纯接口定义）
 * <p>
 * 各电商平台（Amazon、Coupang、Ozon等）的 API 模块需要：
 * <ol>
 * <li>继承此接口</li>
 * <li>添加 @FeignClient 注解指定服务名</li>
 * <li>定义各自的 PREFIX 路径前缀</li>
 * <li>为方法添加 @GetMapping/@PostMapping 等注解</li>
 * </ol>
 * </p>
 * <p>
 * 确保返回统一的 {@link ProductStatisticsDTO} 数据结构
 * </p>
 *
 * @author Jax
 */
public interface PlatformProductStatisticsApi {

    /**
     * 分页查询产品统计数据
     * <p>
     * 按产品维度（ASIN/MSKU/SKU）聚合统计销售、广告、利润等数据
     * </p>
     *
     * @param queryDTO 查询条件
     * @return 产品统计数据分页结果
     */
    CommonResult<PageResult<ProductStatisticsDTO>> getProductStatistics(ProductStatisticsQueryDTO queryDTO);

}
