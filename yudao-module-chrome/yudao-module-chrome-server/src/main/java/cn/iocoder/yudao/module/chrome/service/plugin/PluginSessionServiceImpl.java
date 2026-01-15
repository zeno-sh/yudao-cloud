package cn.iocoder.yudao.module.chrome.service.plugin;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.websocket.core.plugin.PluginSessionManager;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientCookieDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.server.ClientCookieMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 插件会话管理 Service 实现类
 *
 * @author Jax
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PluginSessionServiceImpl implements PluginSessionService {

    private final ClientCookieMapper clientCookieMapper;
    private final PluginSessionManager pluginSessionManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncSessionConnect(String sessionId, String account, String ipAddress) {
        if (StrUtil.isEmpty(account)) {
            log.warn("[PluginSessionService][账号为空,无法同步会话信息][sessionId={}]", sessionId);
            return;
        }

        // 查找是否存在该账号的配置(记录是预设的,不自动创建)
        ClientCookieDO clientCookie = getClientCookieByAccount(account);
        
        if (clientCookie == null) {
            log.warn("[PluginSessionService][账号不存在,无法同步会话信息][account={}][sessionId={}]", account, sessionId);
            return;
        }
        
        // 更新会话信息(通过account查询更新,确保更新正确的记录)
        LambdaUpdateWrapper<ClientCookieDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.and(wrapper -> wrapper
                        .eq(ClientCookieDO::getWsAccount, account)
                        .or()
                        .eq(ClientCookieDO::getUuid, account))
                .set(ClientCookieDO::getWsAccount, account)  // 确保ws_account字段有值
                .set(ClientCookieDO::getWsSessionId, sessionId)
                .set(ClientCookieDO::getWsIpAddress, ipAddress)
                .set(ClientCookieDO::getWsConnectTime, LocalDateTime.now())
                .set(ClientCookieDO::getWsLastHeartbeatTime, LocalDateTime.now())
                .set(ClientCookieDO::getWsOnline, true);
        
        int updateCount = clientCookieMapper.update(null, updateWrapper);
        if (updateCount > 0) {
            log.info("[PluginSessionService][更新会话信息成功][account={}][sessionId={}]", account, sessionId);
        } else {
            log.warn("[PluginSessionService][更新会话信息失败][account={}][sessionId={}]", account, sessionId);
        }
    }

    @Override
    public void updateHeartbeat(String sessionId) {
        if (StrUtil.isEmpty(sessionId)) {
            return;
        }

        LambdaUpdateWrapper<ClientCookieDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ClientCookieDO::getWsSessionId, sessionId)
                .set(ClientCookieDO::getWsLastHeartbeatTime, LocalDateTime.now())
                .set(ClientCookieDO::getWsOnline, true);
        clientCookieMapper.update(null, updateWrapper);
    }

    @Override
    public void updateHeartbeatByAccount(String account, String sessionId) {
        if (StrUtil.isEmpty(account)) {
            return;
        }

        // 通过account查询更新,更可靠
        LambdaUpdateWrapper<ClientCookieDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.and(wrapper -> wrapper
                        .eq(ClientCookieDO::getWsAccount, account)
                        .or()
                        .eq(ClientCookieDO::getUuid, account))
                .set(ClientCookieDO::getWsSessionId, sessionId)  // 同时更新sessionId,防止sessionId变化
                .set(ClientCookieDO::getWsLastHeartbeatTime, LocalDateTime.now())
                .set(ClientCookieDO::getWsOnline, true);
        clientCookieMapper.update(null, updateWrapper);
    }

    @Override
    public void updateStatusData(String sessionId, String statusData) {
        if (StrUtil.isEmpty(sessionId)) {
            return;
        }

        LambdaUpdateWrapper<ClientCookieDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ClientCookieDO::getWsSessionId, sessionId)
                .set(ClientCookieDO::getWsStatusData, statusData)
                .set(ClientCookieDO::getWsLastHeartbeatTime, LocalDateTime.now());
        clientCookieMapper.update(null, updateWrapper);
    }

    @Override
    public void syncSessionDisconnect(String sessionId) {
        if (StrUtil.isEmpty(sessionId)) {
            return;
        }

        LambdaUpdateWrapper<ClientCookieDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ClientCookieDO::getWsSessionId, sessionId)
                .set(ClientCookieDO::getWsOnline, false);
        clientCookieMapper.update(null, updateWrapper);
        log.info("[PluginSessionService][会话断开][sessionId={}]", sessionId);
    }

    @Override
    public ClientCookieDO getClientCookieByAccount(String account) {
        if (StrUtil.isEmpty(account)) {
            return null;
        }

        LambdaQueryWrapper<ClientCookieDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClientCookieDO::getWsAccount, account)
                .or()
                .eq(ClientCookieDO::getUuid, account)
                .last("LIMIT 1");
        return clientCookieMapper.selectOne(queryWrapper);
    }

    @Override
    public ClientCookieDO getClientCookieBySessionId(String sessionId) {
        if (StrUtil.isEmpty(sessionId)) {
            return null;
        }

        LambdaQueryWrapper<ClientCookieDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClientCookieDO::getWsSessionId, sessionId);
        return clientCookieMapper.selectOne(queryWrapper);
    }

    @Override
    public PageResult<ClientCookieDO> getPluginSessionPage(Integer serverId, String account, Boolean online, Integer pageNo, Integer pageSize) {
        // 构建查询条件
        LambdaQueryWrapper<ClientCookieDO> queryWrapper = new LambdaQueryWrapper<>();
        
        // 服务器ID查询
        if (serverId != null) {
            queryWrapper.eq(ClientCookieDO::getServerId, serverId);
        }
        
        // 账号模糊查询
        if (StrUtil.isNotEmpty(account)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(ClientCookieDO::getWsAccount, account)
                    .or()
                    .like(ClientCookieDO::getUuid, account)
            );
        }
        
        // 在线状态查询
        if (online != null) {
            queryWrapper.eq(ClientCookieDO::getWsOnline, online);
        }
        
        // 按最后心跳时间倒序
        queryWrapper.orderByDesc(ClientCookieDO::getWsLastHeartbeatTime);
        
        // 分页查询
        Page<ClientCookieDO> page = new Page<>(pageNo, pageSize);
        Page<ClientCookieDO> resultPage = clientCookieMapper.selectPage(page, queryWrapper);
        
        return new PageResult<>(resultPage.getRecords(), resultPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateOfflineStatus() {
        // 将超过5分钟无心跳的会话标记为离线
        LocalDateTime offlineThreshold = LocalDateTime.now().minusMinutes(5);
        
        LambdaUpdateWrapper<ClientCookieDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ClientCookieDO::getWsOnline, true)
                .lt(ClientCookieDO::getWsLastHeartbeatTime, offlineThreshold)
                .set(ClientCookieDO::getWsOnline, false);
        
        int count = clientCookieMapper.update(null, updateWrapper);
        if (count > 0) {
            log.info("[PluginSessionService][批量更新离线状态][count={}]", count);
        }
    }

}
