package cn.iocoder.yudao.module.chrome.service.email;

import javax.validation.*;
import cn.iocoder.yudao.module.chrome.controller.admin.email.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.email.EmailCodeDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * Chrome邮箱验证码 Service 接口
 *
 * @author Jax
 */
public interface EmailCodeService {

    /**
     * 创建Chrome邮箱验证码
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createEmailCode(@Valid EmailCodeSaveReqVO createReqVO);

    /**
     * 更新Chrome邮箱验证码
     *
     * @param updateReqVO 更新信息
     */
    void updateEmailCode(@Valid EmailCodeSaveReqVO updateReqVO);

    /**
     * 删除Chrome邮箱验证码
     *
     * @param id 编号
     */
    void deleteEmailCode(Long id);

    /**
     * 获得Chrome邮箱验证码
     *
     * @param id 编号
     * @return Chrome邮箱验证码
     */
    EmailCodeDO getEmailCode(Long id);

    /**
     * 获得Chrome邮箱验证码分页
     *
     * @param pageReqVO 分页查询
     * @return Chrome邮箱验证码分页
     */
    PageResult<EmailCodeDO> getEmailCodePage(EmailCodePageReqVO pageReqVO);

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱
     * @param scene 场景（10注册 20忘记密码 30修改密码）
     * @param clientIP 客户端IP
     */
    void sendEmailCode(String email, Integer scene, String clientIP);

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱
     * @param code 验证码
     * @param scene 场景（10注册 20忘记密码 30修改密码）
     */
    void validateEmailCode(String email, String code, Integer scene);

}