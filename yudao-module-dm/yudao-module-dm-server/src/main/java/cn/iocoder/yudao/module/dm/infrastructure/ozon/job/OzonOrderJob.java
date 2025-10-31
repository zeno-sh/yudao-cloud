package cn.iocoder.yudao.module.dm.infrastructure.ozon.job;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.enums.DmPlatformEnum;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.OrderManagerAsyncService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Ozon订单异步同步Job - 解决原有Job卡住问题
 * 
 * 优化点：
 * 1. 使用异步并发处理，避免单线程阻塞
 * 2. 移除while true循环，使用安全的分页逻辑
 * 3. 添加超时控制，防止长时间卡住
 * 4. 单个店铺失败不影响其他店铺
 * 5. 详细的日志记录，便于问题排查
 * 
 * @Author Jax
 * @Date 2025-09-13
 */
@Service("ozonOrderJob")
@Slf4j
public class OzonOrderJob   {

    @Resource
    private OzonShopMappingService dmShopMappingService;
    @Resource
    private OrderManagerAsyncService orderManagerAsyncService;

    @XxlJob("ozonOrderJob")
    @TenantIgnore
    public String execute(String param) throws Exception {
        log.info("[OzonOrderJob]-开始异步同步Ozon订单");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 解析时间参数
            String[] dateRange = parseDateRange(param);
            String beginDate = dateRange[0];
            String endDate = dateRange[1];
            
            log.info("[OzonOrderJob]-同步时间范围: {} 到 {}", beginDate, endDate);

            // 2. 获取店铺列表
            List<OzonShopMappingDO> ozonShopList = getValidOzonShops();
            if (CollectionUtils.isEmpty(ozonShopList)) {
                log.warn("[OzonOrderJob]-暂无有效的Ozon店铺");
                return "暂无有效的Ozon店铺";
            }

            log.info("[OzonOrderJob]-找到{}个Ozon店铺", ozonShopList.size());

            // 3. 异步并发同步所有店铺
            List<CompletableFuture<String>> syncTasks = new ArrayList<>();
            
            for (OzonShopMappingDO shop : ozonShopList) {
                CompletableFuture<String> syncTask = orderManagerAsyncService.syncOrderAsync(shop, beginDate, endDate);
                syncTasks.add(syncTask);
            }

            // 4. 等待所有任务完成，设置总体超时时间
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                syncTasks.toArray(new CompletableFuture[0])
            );
            
            try {
                // 等待所有任务完成，最多等待30分钟
                allTasks.get(30, TimeUnit.MINUTES);
            } catch (java.util.concurrent.TimeoutException e) {
                log.error("[OzonOrderJob]-等待同步任务完成超时(30分钟): {}", e.getMessage());
            } catch (Exception e) {
                log.error("[OzonOrderJob]-等待同步任务完成时发生异常: {}", e.getMessage(), e);
            }

            // 5. 收集所有任务结果
            List<String> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            for (int i = 0; i < syncTasks.size(); i++) {
                CompletableFuture<String> task = syncTasks.get(i);
                OzonShopMappingDO shop = ozonShopList.get(i);
                
                try {
                    if (task.isDone()) {
                        String result = task.get();
                        results.add(result);
                        
                        if (result.contains("成功")) {
                            successCount++;
                        } else {
                            failureCount++;
                        }
                    } else {
                        String timeoutMsg = String.format("店铺[%s]同步超时", shop.getClientId());
                        results.add(timeoutMsg);
                        failureCount++;
                        log.warn("[OzonOrderJob]-{}", timeoutMsg);
                    }
                } catch (Exception e) {
                    String errorMsg = String.format("店铺[%s]获取结果异常: %s", shop.getClientId(), e.getMessage());
                    results.add(errorMsg);
                    failureCount++;
                    log.error("[OzonOrderJob]-{}", errorMsg, e);
                }
            }

            // 6. 生成最终结果
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            String finalResult = String.format(
                "Ozon订单异步同步完成 - 总店铺数: %d, 成功: %d, 失败: %d, 耗时: %d秒",
                ozonShopList.size(), successCount, failureCount, duration / 1000
            );
            
            log.info("[OzonOrderJob]-{}", finalResult);
            
            // 记录详细结果
            for (String result : results) {
                log.info("[OzonOrderJob]-详细结果: {}", result);
            }
            
            return finalResult;
            
        } catch (Exception e) {
            String errorMsg = String.format("Ozon订单异步同步Job执行异常: %s", e.getMessage());
            log.error("[OzonOrderJob]-{}", errorMsg, e);
            return errorMsg;
        }
    }

    /**
     * 解析日期范围参数 - 完全模仿老Job的处理逻辑
     */
    private String[] parseDateRange(String param) {
        DateTime now = DateTime.now();
        DateTime utcNow = DateUtil.offsetHour(now, -8);
        DateTime utcYesterday = DateUtil.offsetDay(utcNow, -1);

        String beginDate = DateUtil.format(utcYesterday, DatePattern.UTC_PATTERN);
        String endDate = DateUtil.format(utcNow, DatePattern.UTC_PATTERN);

        if (StringUtils.isNotBlank(param)) {
            // 入参：2024-02-11,2024-02-12 需要转换成 yyyy-MM-dd'T'HH:mm:ss'Z' 格式
            String[] dateTimes = param.split(",");
            beginDate = DateUtil.format(DateUtil.parseDate(dateTimes[0]), DatePattern.UTC_PATTERN);
            endDate = DateUtil.format(DateUtil.endOfDay(DateUtil.parseDate(dateTimes[1])), DatePattern.UTC_PATTERN);
        }

        return new String[]{beginDate, endDate};
    }

    /**
     * 获取有效的Ozon店铺列表
     */
    private List<OzonShopMappingDO> getValidOzonShops() {
        try {
            List<OzonShopMappingDO> shopList = dmShopMappingService.getOzonShopList();
            if (CollectionUtils.isEmpty(shopList)) {
                return new ArrayList<>();
            }

            // 过滤出Ozon平台的店铺
            List<OzonShopMappingDO> ozonShopList = shopList.stream()
                    .filter(shop -> shop.getPlatform().equals(DmPlatformEnum.OZON.getPlatformId())
                            || shop.getPlatform().equals(DmPlatformEnum.OZON_GLOBAL.getPlatformId()))
                    .collect(Collectors.toList());

            // 过滤出配置完整的店铺
            List<OzonShopMappingDO> validShops = new ArrayList<>();
            for (OzonShopMappingDO shop : ozonShopList) {
                if (StringUtils.isBlank(shop.getClientId()) || StringUtils.isBlank(shop.getApiKey())) {
                    log.warn("[OzonOrderJob]-店铺配置不完整，跳过同步，shopId: {}", shop.getClientId());
                    continue;
                }
                validShops.add(shop);
            }

            return validShops;
            
        } catch (Exception e) {
            log.error("[OzonOrderJob]-获取店铺列表异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
