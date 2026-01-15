package cn.iocoder.yudao.module.chrome.dal.mysql.server;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientServerDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.chrome.controller.admin.server.vo.*;

/**
 * Chrome 插件cookie服务器 Mapper
 *
 * @author Jax
 */
@Mapper
public interface ClientServerMapper extends BaseMapperX<ClientServerDO> {

    default PageResult<ClientServerDO> selectPage(ClientServerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ClientServerDO>()
                .eqIfPresent(ClientServerDO::getIp, reqVO.getIp())
                .betweenIfPresent(ClientServerDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ClientServerDO::getId));
    }

}