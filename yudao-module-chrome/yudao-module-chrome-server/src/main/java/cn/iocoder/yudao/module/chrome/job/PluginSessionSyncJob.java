package cn.iocoder.yudao.module.chrome.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.framework.websocket.core.plugin.PluginSessionManager;
import cn.iocoder.yudao.framework.websocket.core.plugin.PluginUserInfo;
import cn.iocoder.yudao.module.chrome.service.plugin.PluginSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件会话同步定时任务
 *
 * 功能说明：
 * 1. 同步在线会话信息到数据库
 * 2. 检测断开的会话并更新状态
 * 3. 更新心跳时间
 * 4. 批量更新离线状态
 *
 * 注意：使用静态变量保存上次在线会话ID，因为Quartz Job每次执行都会创建新实例
 *
 * @author Jax
 */
@Component
@Slf4j
public class PluginSessionSyncJob implements JobHandler {

    @Resource
    private PluginSessionService pluginSessionService;
    
    @Resource
    private PluginSessionManager pluginSessionManager;

    /**
     * 记录上一次的在线会话ID集合
     * 使用静态变量，因为Quartz Job每次执行都会创建新实例
     * 使用线程安全的Set，支持并发访问
     */
    private static final Set<String> lastOnlineSessionIds = ConcurrentHashMap.newKeySet();

    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        log.debug("[PluginSessionSyncJob][开始执行插件会话同步任务]");

        try {
            // 获取所有在线会话
            Collection<PluginUserInfo> onlinePlugins = pluginSessionManager.getAllPluginUserInfos();
            Set<String> currentOnlineSessionIds = new HashSet<>();
            
            int connectCount = 0;
            int heartbeatCount = 0;
            int disconnectCount = 0;
            
            // 同步在线会话
            for (PluginUserInfo pluginInfo : onlinePlugins) {
                String sessionId = pluginInfo.getSessionId();
                currentOnlineSessionIds.add(sessionId);
                
                try {
                    // 检查是否是新连接
                    if (!lastOnlineSessionIds.contains(sessionId)) {
                        // 新连接,同步连接信息
                        pluginSessionService.syncSessionConnect(
                                sessionId,
                                pluginInfo.getAccount(),
                                pluginInfo.getIpAddress()
                        );
                        connectCount++;
                        log.info("[PluginSessionSyncJob][检测到新连接][account={}][sessionId={}]",
                                pluginInfo.getAccount(), sessionId);
                    } else {
                        // 已存在的连接,通过account更新心跳时间(更可靠)
                        pluginSessionService.updateHeartbeatByAccount(pluginInfo.getAccount(), sessionId);
                        heartbeatCount++;
                    }
                } catch (Exception e) {
                    log.error("[PluginSessionSyncJob][同步会话失败][account={}][sessionId={}]", 
                            pluginInfo.getAccount(), sessionId, e);
                }
            }
            
            // 检测断开的会话
            for (String oldSessionId : lastOnlineSessionIds) {
                if (!currentOnlineSessionIds.contains(oldSessionId)) {
                    try {
                        pluginSessionService.syncSessionDisconnect(oldSessionId);
                        disconnectCount++;
                        log.info("[PluginSessionSyncJob][检测到会话断开][sessionId={}]", oldSessionId);
                    } catch (Exception e) {
                        log.error("[PluginSessionSyncJob][同步断开失败][sessionId={}]", oldSessionId, e);
                    }
                }
            }
            
            // 更新记录
            lastOnlineSessionIds.clear();
            lastOnlineSessionIds.addAll(currentOnlineSessionIds);
            
            // 批量更新离线状态(将长时间无心跳的标记为离线)
            pluginSessionService.batchUpdateOfflineStatus();
            
            String result = String.format("插件会话同步完成 - 在线: %d, 新连接: %d, 心跳: %d, 断开: %d",
                    currentOnlineSessionIds.size(), connectCount, heartbeatCount, disconnectCount);
            log.info("[PluginSessionSyncJob][{}]", result);
            
            return result;

        } catch (Exception e) {
            log.error("[PluginSessionSyncJob][插件会话同步任务执行失败]", e);
            throw e;
        }
    }
}
