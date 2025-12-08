package cn.iocoder.yudao.module.dm.service.platform;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.platform.common.api.PlatformInventoryApi;
import cn.iocoder.yudao.module.platform.common.dto.PlatformInventoryDTO;
import cn.iocoder.yudao.module.platform.common.dto.PlatformInventoryQueryDTO;
import cn.iocoder.yudao.module.platform.common.enums.FulfillmentTypeEnum;
import cn.iocoder.yudao.module.sellfox.api.platform.AmazonInventoryApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 多平台库存聚合服务
 * <p>
 * 通过 Feign 调用各电商平台服务，聚合库存数据
 * 支持 FBA/FBM 筛选
 * </p>
 *
 * @author Jax
 */
@Slf4j
@Service
public class PlatformInventoryAggregateService {

    @Resource
    private AmazonInventoryApi amazonInventoryApi;

    // TODO: 后续添加其他平台
    // @Resource
    // private CoupangInventoryApi coupangInventoryApi;
    // @Resource
    // private OzonInventoryApi ozonInventoryApi;

    /**
     * 平台 API Map，key 为平台 ID（字典 dm_platform 的值）
     */
    private Map<Integer, PlatformInventoryApi> platformApiMap;

    /**
     * 并行调用线程池
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @PostConstruct
    public void init() {
        platformApiMap = new HashMap<>();
        // 平台ID对应字典 dm_platform 的值
        // Amazon: 60
        platformApiMap.put(60, amazonInventoryApi);
        // TODO: 后续添加其他平台
        // Coupang: 50
        // platformApiMap.put(50, coupangInventoryApi);

        log.info("平台库存聚合服务初始化完成，已注册 {} 个平台: {}", platformApiMap.size(), platformApiMap.keySet());
    }

    /**
     * 根据平台 ID 获取 API
     *
     * @param platformId 平台ID
     * @return 平台 API，不存在返回 null
     */
    public PlatformInventoryApi getApiByPlatformId(Integer platformId) {
        return platformApiMap.get(platformId);
    }

    /**
     * 获取所有已注册的平台 ID
     */
    public List<Integer> getRegisteredPlatformIds() {
        return new ArrayList<>(platformApiMap.keySet());
    }

    /**
     * 查询库存数据（支持多平台并行查询）
     *
     * @param queryDTO    查询条件
     * @param platformIds 平台ID列表，为空则查询所有已注册平台
     * @return 库存数据列表
     */
    public List<PlatformInventoryDTO> queryInventory(PlatformInventoryQueryDTO queryDTO, List<Integer> platformIds) {
        List<Integer> targetPlatformIds = (platformIds == null || platformIds.isEmpty())
                ? new ArrayList<>(platformApiMap.keySet())
                : platformIds.stream().filter(platformApiMap::containsKey).collect(Collectors.toList());

        if (targetPlatformIds.isEmpty()) {
            log.warn("没有可查询的平台");
            return Collections.emptyList();
        }

        // 并行调用各平台
        List<CompletableFuture<List<PlatformInventoryDTO>>> futures = new ArrayList<>();
        for (Integer platformId : targetPlatformIds) {
            PlatformInventoryApi api = platformApiMap.get(platformId);
            CompletableFuture<List<PlatformInventoryDTO>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    CommonResult<List<PlatformInventoryDTO>> result = api.queryInventory(queryDTO);
                    if (result != null && result.isSuccess() && result.getData() != null) {
                        // 确保设置 platformId
                        result.getData().forEach(dto -> {
                            if (dto.getPlatformId() == null) {
                                dto.setPlatformId(platformId);
                            }
                        });
                        return result.getData();
                    }
                    log.warn("获取平台 {} 库存数据失败: {}", platformId, result != null ? result.getMsg() : "响应为空");
                } catch (Exception e) {
                    log.error("获取平台 {} 库存数据异常", platformId, e);
                }
                return Collections.<PlatformInventoryDTO>emptyList();
            }, executor);
            futures.add(future);
        }

        // 合并结果
        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * 查询单个平台的库存数据
     *
     * @param platformId 平台ID
     * @param queryDTO   查询条件
     * @return 库存数据列表
     */
    public List<PlatformInventoryDTO> queryInventoryByPlatform(Integer platformId, PlatformInventoryQueryDTO queryDTO) {
        PlatformInventoryApi api = platformApiMap.get(platformId);
        if (api == null) {
            log.warn("未注册的平台ID: {}", platformId);
            return Collections.emptyList();
        }

        try {
            CommonResult<List<PlatformInventoryDTO>> result = api.queryInventory(queryDTO);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
            log.warn("获取平台 {} 库存数据失败: {}", platformId, result != null ? result.getMsg() : "响应为空");
        } catch (Exception e) {
            log.error("获取平台 {} 库存数据异常", platformId, e);
        }
        return Collections.emptyList();
    }

    /**
     * 按产品ID和平台分组查询FBA库存
     *
     * @param localProductIds 本地产品ID列表
     * @param platformIds     平台ID列表，为空则查询所有平台
     * @return Map<本地产品ID, Map<平台ID, FBA库存>>
     */
    public Map<Long, Map<Integer, PlatformInventoryDTO>> queryFbaInventoryByProductIds(List<Long> localProductIds, List<Integer> platformIds) {
        PlatformInventoryQueryDTO queryDTO = new PlatformInventoryQueryDTO();
        queryDTO.setLocalProductIds(localProductIds);
        queryDTO.setFulfillmentType(FulfillmentTypeEnum.FBA.getCode());

        List<PlatformInventoryDTO> inventoryList = queryInventory(queryDTO, platformIds);

        // 按产品ID和平台ID分组
        // Map<产品ID, Map<平台ID, 库存DTO>>
        Map<Long, Map<Integer, PlatformInventoryDTO>> result = new HashMap<>();
        for (PlatformInventoryDTO inv : inventoryList) {
            if (inv.getLocalProductId() == null || inv.getPlatformId() == null) {
                continue;
            }
            result.computeIfAbsent(inv.getLocalProductId(), k -> new HashMap<>())
                    .merge(inv.getPlatformId(), inv, (existing, replacement) -> {
                        // 同一产品同一平台有多条记录时，累加库存
                        int existingAvailable = existing.getAvailableQty() != null ? existing.getAvailableQty() : 0;
                        int replacementAvailable = replacement.getAvailableQty() != null ? replacement.getAvailableQty() : 0;
                        int existingInbound = existing.getInboundQty() != null ? existing.getInboundQty() : 0;
                        int replacementInbound = replacement.getInboundQty() != null ? replacement.getInboundQty() : 0;
                        
                        PlatformInventoryDTO merged = new PlatformInventoryDTO();
                        merged.setLocalProductId(existing.getLocalProductId());
                        merged.setPlatformId(existing.getPlatformId());
                        merged.setAvailableQty(existingAvailable + replacementAvailable);
                        merged.setInboundQty(existingInbound + replacementInbound);
                        return merged;
                    });
        }
        return result;
    }

}
