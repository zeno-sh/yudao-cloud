package cn.iocoder.yudao.module.platform.common.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.platform.common.dto.PlatformOrderDTO;
import cn.iocoder.yudao.module.platform.common.dto.PlatformOrderQueryDTO;

import java.util.List;

/**
 * 平台订单 API 接口规范（纯接口定义）
 * <p>
 * 各电商平台（Amazon、Coupang、Ozon等）的 API 模块需要：
 * 1. 继承此接口
 * 2. 添加 @FeignClient 注解指定服务名
 * 3. 定义各自的 PREFIX 路径前缀
 * 4. 为方法添加 @GetMapping/@PostMapping 等注解
 * </p>
 * <p>
 * 确保返回统一的 {@link PlatformOrderDTO} 数据结构
 * </p>
 * <p>
 * 订单数据通过 fulfillmentType 字段区分：
 * - 1 = FBA（平台履约，如亚马逊FBA、Coupang火箭配送）
 * - 2 = FBM（卖家自发货）
 * </p>
 *
 * @author Jax
 */
public interface PlatformOrderApi {

    /**
     * 查询订单列表
     * <p>
     * 支持按 fulfillmentType 筛选 FBA/FBM 订单
     * </p>
     *
     * @param queryDTO 查询条件
     * @return 订单列表
     */
    CommonResult<List<PlatformOrderDTO>> queryOrders(PlatformOrderQueryDTO queryDTO);

}
