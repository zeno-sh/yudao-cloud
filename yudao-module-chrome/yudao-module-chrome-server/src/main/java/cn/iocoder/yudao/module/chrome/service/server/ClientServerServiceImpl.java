package cn.iocoder.yudao.module.chrome.service.server;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.chrome.controller.admin.server.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientServerDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientCookieDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.chrome.dal.mysql.server.ClientServerMapper;
import cn.iocoder.yudao.module.chrome.dal.mysql.server.ClientCookieMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;

/**
 * Chrome 插件cookie服务器 Service 实现类
 *
 * @author Jax
 */
@Service
@Validated
public class ClientServerServiceImpl implements ClientServerService {

    @Resource
    private ClientServerMapper clientServerMapper;
    @Resource
    private ClientCookieMapper clientCookieMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createClientServer(ClientServerSaveReqVO createReqVO) {
        // 插入
        ClientServerDO clientServer = BeanUtils.toBean(createReqVO, ClientServerDO.class);
        clientServerMapper.insert(clientServer);

        // 插入子表
        createClientCookieList(clientServer.getId(), createReqVO.getClientCookies());
        // 返回
        return clientServer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClientServer(ClientServerSaveReqVO updateReqVO) {
        // 校验存在
        validateClientServerExists(updateReqVO.getId());
        // 更新
        ClientServerDO updateObj = BeanUtils.toBean(updateReqVO, ClientServerDO.class);
        clientServerMapper.updateById(updateObj);

        // 更新子表
        updateClientCookieList(updateReqVO.getId(), updateReqVO.getClientCookies());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteClientServer(Integer id) {
        // 校验存在
        validateClientServerExists(id);
        // 删除
        clientServerMapper.deleteById(id);

        // 删除子表
        deleteClientCookieByServerId(id);
    }

    private void validateClientServerExists(Integer id) {
        if (clientServerMapper.selectById(id) == null) {
            throw exception(CLIENT_SERVER_NOT_EXISTS);
        }
    }

    @Override
    public ClientServerDO getClientServer(Integer id) {
        return clientServerMapper.selectById(id);
    }

    @Override
    public PageResult<ClientServerDO> getClientServerPage(ClientServerPageReqVO pageReqVO) {
        return clientServerMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（Chrome 插件cookie配置） ====================

    @Override
    public List<ClientCookieDO> getClientCookieListByServerId(Integer serverId) {
        return clientCookieMapper.selectListByServerId(serverId);
    }

    private void createClientCookieList(Integer serverId, List<ClientCookieDO> list) {
        list.forEach(o -> o.setServerId(serverId));
        clientCookieMapper.insertBatch(list);
    }

    private void updateClientCookieList(Integer serverId, List<ClientCookieDO> list) {
        deleteClientCookieByServerId(serverId);
		list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createClientCookieList(serverId, list);
    }

    private void deleteClientCookieByServerId(Integer serverId) {
        clientCookieMapper.deleteByServerId(serverId);
    }

}