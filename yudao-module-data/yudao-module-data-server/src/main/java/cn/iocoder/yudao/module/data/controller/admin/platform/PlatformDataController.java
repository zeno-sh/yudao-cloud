package cn.iocoder.yudao.module.data.controller.admin.platform;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.data.service.platform.PlatformDataAggregateService;
import cn.iocoder.yudao.module.platform.common.dto.ShopStatisticsDTO;
import cn.iocoder.yudao.module.platform.common.dto.ShopStatisticsQueryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 多平台数据聚合 Controller
 * <p>
 * 统一入口，聚合查询各电商平台（Amazon、Coupang、Ozon等）的店铺统计数据
 * </p>
 *
 * @author Jax
 */
@Tag(name = "管理后台 - 多平台数据聚合")
@RestController
@RequestMapping("/data/platform")
@Validated
public class PlatformDataController {

    @Resource
    private PlatformDataAggregateService aggregateService;

    @GetMapping("/registered-platforms")
    @Operation(summary = "获取所有已注册的平台")
    public CommonResult<List<Integer>> getRegisteredPlatforms() {
        return success(aggregateService.getRegisteredPlatformIds());
    }

    @PostMapping("/shop-statistics/list")
    @Operation(summary = "查询多平台店铺统计数据")
    public CommonResult<List<ShopStatisticsDTO>> listShopStatistics(@RequestBody ShopStatisticsQueryDTO queryDTO) {
        return success(aggregateService.getShopStatistics(queryDTO));
    }

}
