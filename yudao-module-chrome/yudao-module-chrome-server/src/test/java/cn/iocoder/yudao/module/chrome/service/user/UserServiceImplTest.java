package cn.iocoder.yudao.module.chrome.service.user;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.chrome.controller.admin.user.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.user.ChromeUserMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link UserServiceImpl} 的单元测试类
 *
 * @author Jax
 */
@Import(UserServiceImpl.class)
public class UserServiceImplTest extends BaseDbUnitTest {

    @Resource
    private UserServiceImpl userService;

    @Resource
    private ChromeUserMapper chromeUserMapper;

    @Test
    public void testCreateUser_success() {
        // 准备参数
        UserSaveReqVO createReqVO = randomPojo(UserSaveReqVO.class).setId(null);

        // 调用
        Long userId = userService.createUser(createReqVO);
        // 断言
        assertNotNull(userId);
        // 校验记录的属性是否正确
        UserDO user = chromeUserMapper.selectById(userId);
        assertPojoEquals(createReqVO, user, "id");
    }

    @Test
    public void testUpdateUser_success() {
        // mock 数据
        UserDO dbUser = randomPojo(UserDO.class);
        chromeUserMapper.insert(dbUser);// @Sql: 先插入出一条存在的数据
        // 准备参数
        UserSaveReqVO updateReqVO = randomPojo(UserSaveReqVO.class, o -> {
            o.setId(dbUser.getId()); // 设置更新的 ID
        });

        // 调用
        userService.updateUser(updateReqVO);
        // 校验是否更新正确
        UserDO user = chromeUserMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, user);
    }

    @Test
    public void testUpdateUser_notExists() {
        // 准备参数
        UserSaveReqVO updateReqVO = randomPojo(UserSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> userService.updateUser(updateReqVO), USER_NOT_EXISTS);
    }

    @Test
    public void testDeleteUser_success() {
        // mock 数据
        UserDO dbUser = randomPojo(UserDO.class);
        chromeUserMapper.insert(dbUser);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbUser.getId();

        // 调用
        userService.deleteUser(id);
       // 校验数据不存在了
       assertNull(chromeUserMapper.selectById(id));
    }

    @Test
    public void testDeleteUser_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> userService.deleteUser(id), USER_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetUserPage() {
       // mock 数据
       UserDO dbUser = randomPojo(UserDO.class, o -> { // 等会查询到
           o.setEmail(null);
           o.setPassword(null);
           o.setNickname(null);
           o.setStatus(null);
           o.setLoginIp(null);
           o.setLoginDate(null);
           o.setDeviceToken(null);
           o.setCreateTime(null);
       });
       chromeUserMapper.insert(dbUser);
       // 测试 email 不匹配
       chromeUserMapper.insert(cloneIgnoreId(dbUser, o -> o.setEmail(null)));
       // 测试 password 不匹配
       chromeUserMapper.insert(cloneIgnoreId(dbUser, o -> o.setPassword(null)));
       // 测试 nickname 不匹配
       chromeUserMapper.insert(cloneIgnoreId(dbUser, o -> o.setNickname(null)));
       // 测试 status 不匹配
       chromeUserMapper.insert(cloneIgnoreId(dbUser, o -> o.setStatus(null)));
       // 测试 loginIp 不匹配
       chromeUserMapper.insert(cloneIgnoreId(dbUser, o -> o.setLoginIp(null)));
       // 测试 loginDate 不匹配
       chromeUserMapper.insert(cloneIgnoreId(dbUser, o -> o.setLoginDate(null)));
       // 测试 deviceToken 不匹配
       chromeUserMapper.insert(cloneIgnoreId(dbUser, o -> o.setDeviceToken(null)));
       // 测试 createTime 不匹配
       chromeUserMapper.insert(cloneIgnoreId(dbUser, o -> o.setCreateTime(null)));
       // 准备参数
       UserPageReqVO reqVO = new UserPageReqVO();
       reqVO.setEmail(null);
       reqVO.setPassword(null);
       reqVO.setNickname(null);
       reqVO.setStatus(null);
       reqVO.setLoginIp(null);
       reqVO.setLoginDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDeviceToken(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<UserDO> pageResult = userService.getUserPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbUser, pageResult.getList().get(0));
    }

}