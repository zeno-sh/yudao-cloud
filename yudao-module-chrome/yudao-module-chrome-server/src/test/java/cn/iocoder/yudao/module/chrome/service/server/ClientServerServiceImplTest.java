package cn.iocoder.yudao.module.chrome.service.server;

import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.chrome.controller.admin.server.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientServerDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.server.ClientServerMapper;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.CLIENT_SERVER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ClientServerServiceImpl} 的单元测试类
 *
 * @author Jax
 */
@Import(ClientServerServiceImpl.class)
public class ClientServerServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ClientServerServiceImpl clientServerService;

    @Resource
    private ClientServerMapper clientServerMapper;

    @Test
    public void testCreateClientServer_success() {
        // 准备参数
        ClientServerSaveReqVO createReqVO = randomPojo(ClientServerSaveReqVO.class).setId(null);

        // 调用
        Integer clientServerId = clientServerService.createClientServer(createReqVO);
        // 断言
        assertNotNull(clientServerId);
        // 校验记录的属性是否正确
        ClientServerDO clientServer = clientServerMapper.selectById(clientServerId);
        assertPojoEquals(createReqVO, clientServer, "id");
    }

    @Test
    public void testUpdateClientServer_success() {
        // mock 数据
        ClientServerDO dbClientServer = randomPojo(ClientServerDO.class);
        clientServerMapper.insert(dbClientServer);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ClientServerSaveReqVO updateReqVO = randomPojo(ClientServerSaveReqVO.class, o -> {
            o.setId(dbClientServer.getId()); // 设置更新的 ID
        });

        // 调用
        clientServerService.updateClientServer(updateReqVO);
        // 校验是否更新正确
        ClientServerDO clientServer = clientServerMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, clientServer);
    }

    @Test
    public void testUpdateClientServer_notExists() {
        // 准备参数
        ClientServerSaveReqVO updateReqVO = randomPojo(ClientServerSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> clientServerService.updateClientServer(updateReqVO), CLIENT_SERVER_NOT_EXISTS);
    }

    @Test
    public void testDeleteClientServer_success() {
        // mock 数据
        ClientServerDO dbClientServer = randomPojo(ClientServerDO.class);
        clientServerMapper.insert(dbClientServer);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Integer id = dbClientServer.getId();

        // 调用
        clientServerService.deleteClientServer(id);
       // 校验数据不存在了
       assertNull(clientServerMapper.selectById(id));
    }


}