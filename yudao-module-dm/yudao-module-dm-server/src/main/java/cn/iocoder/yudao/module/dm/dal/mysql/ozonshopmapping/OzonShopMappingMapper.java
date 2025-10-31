package cn.iocoder.yudao.module.dm.dal.mysql.ozonshopmapping;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.dm.controller.admin.shop.vo.*;

/**
 * ozon店铺 Mapper
 *
 * @author zeno
 */
@Mapper
public interface OzonShopMappingMapper extends BaseMapperX<OzonShopMappingDO> {

    default PageResult<OzonShopMappingDO> selectPage(OzonShopMappingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OzonShopMappingDO>()
                .eqIfPresent(OzonShopMappingDO::getPlatform, reqVO.getPlatform())
                .likeIfPresent(OzonShopMappingDO::getShopName, reqVO.getShopName())
                .eqIfPresent(OzonShopMappingDO::getClientId, reqVO.getClientId())
                .eqIfPresent(OzonShopMappingDO::getAuthStatus, reqVO.getAuthStatus())
                .betweenIfPresent(OzonShopMappingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OzonShopMappingDO::getId));
    }

}