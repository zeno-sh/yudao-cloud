package cn.iocoder.yudao.module.chrome.service.user;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.chrome.controller.admin.user.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 用户 Service 接口
 *
 * @author Jax
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUser(@Valid UserSaveReqVO createReqVO);

    /**
     * 更新用户
     *
     * @param updateReqVO 更新信息
     */
    void updateUser(@Valid UserSaveReqVO updateReqVO);

    /**
     * 更新用户
     *
     * @param updateObj 更新对象
     */
    void updateUser(UserDO updateObj);

    /**
     * 删除用户
     *
     * @param id 编号
     */
    void deleteUser(Long id);

    /**
     * 获得用户
     *
     * @param id 编号
     * @return 用户
     */
    UserDO getUser(Long id);

    /**
     * 获得用户分页
     *
     * @param pageReqVO 分页查询
     * @return 用户分页
     */
    PageResult<UserDO> getUserPage(UserPageReqVO pageReqVO);

    /**
     * 根据邮箱获得用户
     *
     * @param email 邮箱
     * @return 用户
     */
    UserDO getUserByEmail(String email);

    /**
     * 更新用户密码
     *
     * @param id 用户ID
     * @param password 新密码
     */
    void updateUserPassword(Long id, String password);

    /**
     * 获取用户信息（包含订阅和积分信息）
     *
     * @param id 用户ID
     * @return 用户信息
     */
    cn.iocoder.yudao.module.chrome.controller.plugin.user.vo.UserInfoRespVO getUserInfo(Long id);

    /**
     * 根据邮箱获取用户信息
     *
     * @param email 邮箱
     * @return 用户信息
     */
    cn.iocoder.yudao.module.chrome.controller.plugin.user.vo.UserInfoRespVO getUserInfoByEmail(String email);

    /**
     * 检查设备令牌是否已存在
     *
     * @param deviceToken 设备令牌
     * @return 是否存在
     */
    boolean isDeviceTokenExists(String deviceToken);

}