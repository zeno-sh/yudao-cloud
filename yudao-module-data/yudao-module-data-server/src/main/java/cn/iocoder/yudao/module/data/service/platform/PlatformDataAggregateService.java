package cn.iocoder.yudao.module.data.service.platform;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.dm.api.DmShopMappingQueryService;
import cn.iocoder.yudao.module.dm.dto.ShopMappingDTO;
import cn.iocoder.yudao.module.platform.common.api.PlatformShopStatisticsApi;
import cn.iocoder.yudao.module.platform.common.dto.ShopStatisticsDTO;
import cn.iocoder.yudao.module.platform.common.dto.ShopStatisticsQueryDTO;
import cn.iocoder.yudao.module.sellfox.api.statistics.AmazonShopStatisticsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 多平台数据聚合服务
 * <p>
 * 通过 Feign 调用各电商平台服务，聚合店铺统计数据
 * </p>
 *
 * @author Jax
 */
@Slf4j
@Service
public class PlatformDataAggregateService {

    @Resource
    private AmazonShopStatisticsApi amazonShopStatisticsApi;

    @Resource
    private DmShopMappingQueryService dmShopMappingQueryService;

    // TODO: 后续添加其他平台
    // @Resource
    // private OzonShopStatisticsApi ozonShopStatisticsApi;
    // @Resource
    // private CoupangShopStatisticsApi coupangShopStatisticsApi;

    /**
     * 平台 API Map，key 为平台 ID（字典 dm_platform 的值）
     */
    private Map<Integer, PlatformShopStatisticsApi> platformApiMap;

    /**
     * 并行调用线程池
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @PostConstruct
    public void init() {
        platformApiMap = new HashMap<>();
        // 平台ID对应字典 dm_platform 的值
        // Amazon: 1
        platformApiMap.put(60, amazonShopStatisticsApi);

        log.info("平台数据聚合服务初始化完成，已注册 {} 个平台: {}", platformApiMap.size(), platformApiMap.keySet());
    }

    /**
     * 根据平台 ID 获取 API
     *
     * @param platformId 平台ID
     * @return 平台 API，不存在返回 null
     */
    public PlatformShopStatisticsApi getApiByPlatformId(Integer platformId) {
        return platformApiMap.get(platformId);
    }

    /**
     * 获取所有已注册的平台 ID
     */
    public List<Integer> getRegisteredPlatformIds() {
        return new ArrayList<>(platformApiMap.keySet());
    }

    /**
     * 查询店铺统计数据
     *
     * @param platformIds 平台ID列表，为空则查询所有已注册平台
     * @param queryDTO    查询条件
     * @return 店铺统计数据列表
     */
    public List<ShopStatisticsDTO> getShopStatistics(List<Integer> platformIds, ShopStatisticsQueryDTO queryDTO) {
        List<Integer> targetPlatformIds = (platformIds == null || platformIds.isEmpty())
                ? new ArrayList<>(platformApiMap.keySet())
                : platformIds.stream().filter(platformApiMap::containsKey).collect(Collectors.toList());

        // 并行调用各平台
        List<CompletableFuture<List<ShopStatisticsDTO>>> futures = new ArrayList<>();
        for (Integer platformId : targetPlatformIds) {
            PlatformShopStatisticsApi api = platformApiMap.get(platformId);
            CompletableFuture<List<ShopStatisticsDTO>> future = CompletableFuture.supplyAsync(() -> {
                CommonResult<List<ShopStatisticsDTO>> result = api.getShopStatistics(queryDTO);
                if (result != null && result.isSuccess() && result.getData() != null) {
                    result.getData().forEach(dto -> dto.setPlatformId(platformId));
                    return result.getData();
                }
                log.warn("获取平台 {} 店铺统计数据失败: {}", platformId, result != null ? result.getMsg() : "响应为空");
                return Collections.<ShopStatisticsDTO>emptyList();
            }, executor);
            futures.add(future);
        }

        // 合并结果
        List<ShopStatisticsDTO> result = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // 填充门店名称和平台名称
        enrichShopInfo(result);

        return result;
    }

    /**
     * 填充门店名称和平台名称
     *
     * @param statisticsList 统计数据列表
     */
    private void enrichShopInfo(List<ShopStatisticsDTO> statisticsList) {
        if (CollectionUtils.isEmpty(statisticsList)) {
            return;
        }

        // 收集所有 shopId（即 clientId）
        List<String> clientIds = statisticsList.stream()
                .map(ShopStatisticsDTO::getShopId)
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
        for (ShopStatisticsDTO dto : statisticsList) {
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
