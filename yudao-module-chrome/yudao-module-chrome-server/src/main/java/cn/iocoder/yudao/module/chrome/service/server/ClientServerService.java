package cn.iocoder.yudao.module.chrome.service.server;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.chrome.controller.admin.server.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientServerDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientCookieDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * Chrome 插件cookie服务器 Service 接口
 *
 * @author Jax
 */
public interface ClientServerService {

    /**
     * 创建Chrome 插件cookie服务器
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createClientServer(@Valid ClientServerSaveReqVO createReqVO);

    /**
     * 更新Chrome 插件cookie服务器
     *
     * @param updateReqVO 更新信息
     */
    void updateClientServer(@Valid ClientServerSaveReqVO updateReqVO);

    /**
     * 删除Chrome 插件cookie服务器
     *
     * @param id 编号
     */
    void deleteClientServer(Integer id);

    /**
     * 获得Chrome 插件cookie服务器
     *
     * @param id 编号
     * @return Chrome 插件cookie服务器
     */
    ClientServerDO getClientServer(Integer id);

    /**
     * 获得Chrome 插件cookie服务器分页
     *
     * @param pageReqVO 分页查询
     * @return Chrome 插件cookie服务器分页
     */
    PageResult<ClientServerDO> getClientServerPage(ClientServerPageReqVO pageReqVO);

    // ==================== 子表（Chrome 插件cookie配置） ====================

    /**
     * 获得Chrome 插件cookie配置列表
     *
     * @param serverId 服务器id
     * @return Chrome 插件cookie配置列表
     */
    List<ClientCookieDO> getClientCookieListByServerId(Integer serverId);

}