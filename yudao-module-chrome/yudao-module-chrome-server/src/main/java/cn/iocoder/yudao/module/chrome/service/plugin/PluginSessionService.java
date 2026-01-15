package cn.iocoder.yudao.module.chrome.service.plugin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientCookieDO;

import javax.validation.Valid;

/**
 * 插件会话管理 Service 接口
 *
 * @author Jax
 */
public interface PluginSessionService {

    /**
     * 同步WebSocket会话信息到数据库
     * 当插件连接时调用
     *
     * @param sessionId WebSocket会话ID
     * @param account 账号
     * @param ipAddress IP地址
     */
    void syncSessionConnect(String sessionId, String account, String ipAddress);

    /**
     * 更新心跳时间(通过SessionId)
     *
     * @param sessionId WebSocket会话ID
     */
    void updateHeartbeat(String sessionId);

    /**
     * 更新心跳时间(通过Account)
     *
     * @param account 账号
     * @param sessionId WebSocket会话ID
     */
    void updateHeartbeatByAccount(String account, String sessionId);

    /**
     * 更新状态数据
     *
     * @param sessionId WebSocket会话ID
     * @param statusData 状态数据(JSON格式)
     */
    void updateStatusData(String sessionId, String statusData);

    /**
     * 同步会话断开
     *
     * @param sessionId WebSocket会话ID
     */
    void syncSessionDisconnect(String sessionId);

    /**
     * 根据账号获取Cookie配置
     *
     * @param account 账号
     * @return Cookie配置
     */
    ClientCookieDO getClientCookieByAccount(String account);

    /**
     * 根据SessionId获取Cookie配置
     *
     * @param sessionId WebSocket会话ID
     * @return Cookie配置
     */
    ClientCookieDO getClientCookieBySessionId(String sessionId);

    /**
     * 获取插件会话分页列表
     *
     * @param serverId 服务器ID(可选)
     * @param account 账号(可选,支持模糊查询)
     * @param online 在线状态(可选)
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 插件会话分页列表
     */
    PageResult<ClientCookieDO> getPluginSessionPage(Integer serverId, String account, Boolean online, Integer pageNo, Integer pageSize);

    /**
     * 批量更新离线状态
     * 定时任务调用,将长时间无心跳的会话标记为离线
     */
    void batchUpdateOfflineStatus();

}
