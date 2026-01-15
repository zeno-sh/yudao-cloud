package cn.iocoder.yudao.module.dm.service.platform;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.platform.common.api.PlatformOrderApi;
import cn.iocoder.yudao.module.platform.common.dto.PlatformOrderDTO;
import cn.iocoder.yudao.module.platform.common.dto.PlatformOrderQueryDTO;
import cn.iocoder.yudao.module.sellfox.api.statistics.AmazonOrderApi;
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
 * 多平台订单聚合服务
 * <p>
 * 通过 Feign 调用各电商平台服务，聚合订单数据
 * 支持 FBA/FBM 筛选
 * </p>
 *
 * @author Jax
 */
@Slf4j
@Service
public class PlatformOrderAggregateService {

    @Resource
    private AmazonOrderApi amazonOrderApi;

    // TODO: 后续添加其他平台
    // @Resource
    // private CoupangOrderApi coupangOrderApi;
    // @Resource
    // private OzonOrderApi ozonOrderApi;

    /**
     * 平台 API Map，key 为平台 ID（字典 dm_platform 的值）
     */
    private Map<Integer, PlatformOrderApi> platformApiMap;

    /**
     * 并行调用线程池
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @PostConstruct
    public void init() {
        platformApiMap = new HashMap<>();
        // 平台ID对应字典 dm_platform 的值
        // Amazon: 60
        platformApiMap.put(60, amazonOrderApi);
        // TODO: 后续添加其他平台
        // Coupang: 50
        // platformApiMap.put(50, coupangOrderApi);

        log.info("平台订单聚合服务初始化完成，已注册 {} 个平台: {}", platformApiMap.size(), platformApiMap.keySet());
    }

    /**
     * 根据平台 ID 获取 API
     *
     * @param platformId 平台ID
     * @return 平台 API，不存在返回 null
     */
    public PlatformOrderApi getApiByPlatformId(Integer platformId) {
        return platformApiMap.get(platformId);
    }

    /**
     * 获取所有已注册的平台 ID
     */
    public List<Integer> getRegisteredPlatformIds() {
        return new ArrayList<>(platformApiMap.keySet());
    }

    /**
     * 查询订单数据（支持多平台并行查询）
     *
     * @param queryDTO    查询条件
     * @param platformIds 平台ID列表，为空则查询所有已注册平台
     * @return 订单数据列表
     */
    public List<PlatformOrderDTO> queryOrders(PlatformOrderQueryDTO queryDTO, List<Integer> platformIds) {
        List<Integer> targetPlatformIds = (platformIds == null || platformIds.isEmpty())
                ? new ArrayList<>(platformApiMap.keySet())
                : platformIds.stream().filter(platformApiMap::containsKey).collect(Collectors.toList());

        if (targetPlatformIds.isEmpty()) {
            log.warn("没有可查询的平台");
            return Collections.emptyList();
        }

        // 并行调用各平台
        List<CompletableFuture<List<PlatformOrderDTO>>> futures = new ArrayList<>();
        for (Integer platformId : targetPlatformIds) {
            PlatformOrderApi api = platformApiMap.get(platformId);
            CompletableFuture<List<PlatformOrderDTO>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    CommonResult<List<PlatformOrderDTO>> result = api.queryOrders(queryDTO);
                    if (result != null && result.isSuccess() && result.getData() != null) {
                        // 确保设置 platformId
                        result.getData().forEach(dto -> {
                            if (dto.getPlatformId() == null) {
                                dto.setPlatformId(platformId);
                            }
                        });
                        return result.getData();
                    }
                    log.warn("获取平台 {} 订单数据失败: {}", platformId, result != null ? result.getMsg() : "响应为空");
                } catch (Exception e) {
                    log.error("获取平台 {} 订单数据异常", platformId, e);
                }
                return Collections.<PlatformOrderDTO>emptyList();
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
     * 查询单个平台的订单数据
     *
     * @param platformId 平台ID
     * @param queryDTO   查询条件
     * @return 订单数据列表
     */
    public List<PlatformOrderDTO> queryOrdersByPlatform(Integer platformId, PlatformOrderQueryDTO queryDTO) {
        PlatformOrderApi api = platformApiMap.get(platformId);
        if (api == null) {
            log.warn("未注册的平台ID: {}", platformId);
            return Collections.emptyList();
        }

        try {
            CommonResult<List<PlatformOrderDTO>> result = api.queryOrders(queryDTO);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
            log.warn("获取平台 {} 订单数据失败: {}", platformId, result != null ? result.getMsg() : "响应为空");
        } catch (Exception e) {
            log.error("获取平台 {} 订单数据异常", platformId, e);
        }
        return Collections.emptyList();
    }

}
