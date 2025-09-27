package cn.iocoder.yudao.module.dm.dal.mysql.online;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.online.OzonProductOnlineDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.online.vo.*;

/**
 * 在线商品 Mapper
 *
 * @author Zeno
 */
@Mapper
public interface OzonProductOnlineMapper extends BaseMapperX<OzonProductOnlineDO> {

    default PageResult<OzonProductOnlineDO> selectPage(OzonProductOnlinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OzonProductOnlineDO>()
                .inIfPresent(OzonProductOnlineDO::getClientId, reqVO.getClientIds())
                .eqIfPresent(OzonProductOnlineDO::getOfferId, reqVO.getOfferId())
                .eqIfPresent(OzonProductOnlineDO::getStatus, reqVO.getStatus())
                .orderByDesc(OzonProductOnlineDO::getId));
    }

}