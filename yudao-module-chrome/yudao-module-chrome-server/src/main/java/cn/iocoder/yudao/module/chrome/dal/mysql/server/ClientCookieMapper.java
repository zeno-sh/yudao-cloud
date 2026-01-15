package cn.iocoder.yudao.module.chrome.dal.mysql.server;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientCookieDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Chrome 插件cookie配置 Mapper
 *
 * @author Jax
 */
@Mapper
public interface ClientCookieMapper extends BaseMapperX<ClientCookieDO> {

    default List<ClientCookieDO> selectListByServerId(Integer serverId) {
        return selectList(ClientCookieDO::getServerId, serverId);
    }

    default int deleteByServerId(Integer serverId) {
        return delete(ClientCookieDO::getServerId, serverId);
    }

}