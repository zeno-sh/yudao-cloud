package cn.iocoder.yudao.module.platform.common.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.platform.common.dto.PlatformInventoryDTO;
import cn.iocoder.yudao.module.platform.common.dto.PlatformInventoryQueryDTO;

import java.util.List;

/**
 * 平台库存 API 接口规范（纯接口定义）
 * <p>
 * 各电商平台（Amazon、Coupang、Ozon等）的 API 模块需要：
 * 1. 继承此接口
 * 2. 添加 @FeignClient 注解指定服务名
 * 3. 定义各自的 PREFIX 路径前缀
 * 4. 为方法添加 @GetMapping/@PostMapping 等注解
 * </p>
 * <p>
 * 确保返回统一的 {@link PlatformInventoryDTO} 数据结构
 * </p>
 * <p>
 * 库存数据通过 fulfillmentType 字段区分：
 * - 1 = FBA（平台仓库存，如亚马逊FBA仓库）
 * - 2 = FBM（海外仓库存、自有仓库存）
 * </p>
 *
 * @author Jax
 */
public interface PlatformInventoryApi {

    /**
     * 查询库存列表
     * <p>
     * 支持按 fulfillmentType 筛选 FBA/FBM 库存
     * </p>
     *
     * @param queryDTO 查询条件
     * @return 库存列表
     */
    CommonResult<List<PlatformInventoryDTO>> queryInventory(PlatformInventoryQueryDTO queryDTO);

}
