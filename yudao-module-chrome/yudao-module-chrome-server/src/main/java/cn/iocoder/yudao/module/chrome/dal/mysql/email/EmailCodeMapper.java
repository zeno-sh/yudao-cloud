package cn.iocoder.yudao.module.chrome.dal.mysql.email;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.chrome.dal.dataobject.email.EmailCodeDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.chrome.controller.admin.email.vo.*;

/**
 * Chrome邮箱验证码 Mapper
 *
 * @author Jax
 */
@Mapper
public interface EmailCodeMapper extends BaseMapperX<EmailCodeDO> {

    default PageResult<EmailCodeDO> selectPage(EmailCodePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EmailCodeDO>()
                .eqIfPresent(EmailCodeDO::getEmail, reqVO.getEmail())
                .eqIfPresent(EmailCodeDO::getCode, reqVO.getCode())
                .eqIfPresent(EmailCodeDO::getScene, reqVO.getScene())
                .eqIfPresent(EmailCodeDO::getUsed, reqVO.getUsed())
                .betweenIfPresent(EmailCodeDO::getExpireTime, reqVO.getExpireTime())
                .betweenIfPresent(EmailCodeDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EmailCodeDO::getId));
    }

}