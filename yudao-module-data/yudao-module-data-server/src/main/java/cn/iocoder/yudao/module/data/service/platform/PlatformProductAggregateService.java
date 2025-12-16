package cn.iocoder.yudao.module.data.service.platform;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.api.DmShopMappingQueryService;
import cn.iocoder.yudao.module.dm.dto.ShopMappingDTO;
import cn.iocoder.yudao.module.platform.common.api.PlatformProductStatisticsApi;
import cn.iocoder.yudao.module.platform.common.dto.ProductStatisticsDTO;
import cn.iocoder.yudao.module.platform.common.dto.ProductStatisticsQueryDTO;
import cn.iocoder.yudao.module.sellfox.api.product.AmazonProductApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 多平台产品数据聚合服务
 * <p>
 * 通过 Feign 调用各电商平台服务，聚合产品统计数据
 * </p>
 *
 * @author Jax
 */
@Slf4j
@Service
public class PlatformProductAggregateService {

    @Resource
    private AmazonProductApi amazonProductApi;

    @Resource
    private DmShopMappingQueryService dmShopMappingQueryService;

    /**
     * 平台 API Map，key 为平台 ID（字典 dm_platform 的值）
     */
    private Map<Integer, PlatformProductStatisticsApi> platformApiMap;

    @PostConstruct
    public void init() {
        platformApiMap = new HashMap<>();
        // 平台ID对应字典 dm_platform 的值
        // Amazon: 60
        platformApiMap.put(60, amazonProductApi);

        log.info("平台产品数据聚合服务初始化完成，已注册 {} 个平台: {}", platformApiMap.size(), platformApiMap.keySet());
    }

    /**
     * 根据平台 ID 获取 API
     *
     * @param platformId 平台ID
     * @return 平台 API，不存在返回 null
     */
    public PlatformProductStatisticsApi getApiByPlatformId(Integer platformId) {
        return platformApiMap.get(platformId);
    }

    /**
     * 获取所有已注册的平台 ID
     */
    public List<Integer> getRegisteredPlatformIds() {
        return new ArrayList<>(platformApiMap.keySet());
    }

    /**
     * 查询产品统计数据
     * <p>
     * 注意：产品统计数据需要分页，因此不支持同时查询多个平台，
     * 如果传入多个平台ID，将只查询第一个平台
     * </p>
     *
     * @param queryDTO 查询条件（包含 platformIds，必须指定平台）
     * @return 产品统计数据分页结果
     */
    public PageResult<ProductStatisticsDTO> getProductStatistics(ProductStatisticsQueryDTO queryDTO) {
        List<Integer> platformIds = queryDTO.getPlatformIds();
        if (CollectionUtils.isEmpty(platformIds)) {
            log.warn("未指定平台ID，返回空结果");
            return new PageResult<>(new ArrayList<>(), 0L);
        }

        // 产品维度数据需要分页，只支持单平台查询
        Integer platformId = platformIds.get(0);
        PlatformProductStatisticsApi api = platformApiMap.get(platformId);
        if (api == null) {
            log.warn("平台 {} 未注册产品统计 API", platformId);
            return new PageResult<>(new ArrayList<>(), 0L);
        }

        CommonResult<PageResult<ProductStatisticsDTO>> result = api.getProductStatistics(queryDTO);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            log.warn("获取平台 {} 产品统计数据失败: {}", platformId, result != null ? result.getMsg() : "响应为空");
            return new PageResult<>(new ArrayList<>(), 0L);
        }

        PageResult<ProductStatisticsDTO> pageResult = result.getData();
        // 填充平台ID
        if (!CollectionUtils.isEmpty(pageResult.getList())) {
            pageResult.getList().forEach(dto -> dto.setPlatformId(platformId));
        }

        // 填充门店名称和平台名称
        enrichProductInfo(pageResult.getList());

        return pageResult;
    }

    /**
     * 填充门店名称和平台名称
     *
     * @param statisticsList 统计数据列表
     */
    private void enrichProductInfo(List<ProductStatisticsDTO> statisticsList) {
        if (CollectionUtils.isEmpty(statisticsList)) {
            return;
        }

        // 收集所有 shopId（即 clientId）
        List<String> clientIds = statisticsList.stream()
                .map(ProductStatisticsDTO::getShopId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(clientIds)) {
            return;
        }

        // 批量查询门店映射信息
        CommonResult<List<ShopMappingDTO>> result = dmShopMappingQueryService.batchGetShopMappingByClientIds(clientIds);
        if (result == null || !result.isSuccess() || CollectionUtils.isEmpty(result.getData())) {
            log.warn("获取门店映射信息失败或为空, clientIds: {}", clientIds);
            return;
        }

        // 构建 clientId -> ShopMappingDTO 映射
        Map<String, ShopMappingDTO> shopMappingMap = result.getData().stream()
                .collect(Collectors.toMap(ShopMappingDTO::getClientId, dto -> dto, (a, b) -> a));

        // 填充 shopName 和 platformName
        for (ProductStatisticsDTO dto : statisticsList) {
            String shopId = dto.getShopId();
            if (shopId == null || shopId.isEmpty()) {
                continue;
            }

            ShopMappingDTO shopMapping = shopMappingMap.get(shopId);
            if (shopMapping != null) {
                dto.setShopName(shopMapping.getShopName());
                // 获取平台名称（从字典）
                String platformName = DictFrameworkUtils.parseDictDataValue(
                        "dm_platform", String.valueOf(shopMapping.getPlatform()));
                dto.setPlatformName(platformName);
            }
        }
    }

}
