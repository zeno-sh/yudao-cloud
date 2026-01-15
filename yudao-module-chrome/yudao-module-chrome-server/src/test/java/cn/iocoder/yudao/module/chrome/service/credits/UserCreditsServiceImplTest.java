package cn.iocoder.yudao.module.chrome.service.credits;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.chrome.controller.admin.credits.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.credits.UserCreditsDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.credits.UserCreditsMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link UserCreditsServiceImpl} 的单元测试类
 *
 * @author Jax
 */
@Import(UserCreditsServiceImpl.class)
public class UserCreditsServiceImplTest extends BaseDbUnitTest {

    @Resource
    private UserCreditsServiceImpl userCreditsService;

    @Resource
    private UserCreditsMapper userCreditsMapper;

    @Test
    public void testCreateUserCredits_success() {
        // 准备参数
        UserCreditsSaveReqVO createReqVO = randomPojo(UserCreditsSaveReqVO.class).setId(null);

        // 调用
        Long userCreditsId = userCreditsService.createUserCredits(createReqVO);
        // 断言
        assertNotNull(userCreditsId);
        // 校验记录的属性是否正确
        UserCreditsDO userCredits = userCreditsMapper.selectById(userCreditsId);
        assertPojoEquals(createReqVO, userCredits, "id");
    }

    @Test
    public void testUpdateUserCredits_success() {
        // mock 数据
        UserCreditsDO dbUserCredits = randomPojo(UserCreditsDO.class);
        userCreditsMapper.insert(dbUserCredits);// @Sql: 先插入出一条存在的数据
        // 准备参数
        UserCreditsSaveReqVO updateReqVO = randomPojo(UserCreditsSaveReqVO.class, o -> {
            o.setId(dbUserCredits.getId()); // 设置更新的 ID
        });

        // 调用
        userCreditsService.updateUserCredits(updateReqVO);
        // 校验是否更新正确
        UserCreditsDO userCredits = userCreditsMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, userCredits);
    }

    @Test
    public void testUpdateUserCredits_notExists() {
        // 准备参数
        UserCreditsSaveReqVO updateReqVO = randomPojo(UserCreditsSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> userCreditsService.updateUserCredits(updateReqVO), USER_CREDITS_NOT_EXISTS);
    }

    @Test
    public void testDeleteUserCredits_success() {
        // mock 数据
        UserCreditsDO dbUserCredits = randomPojo(UserCreditsDO.class);
        userCreditsMapper.insert(dbUserCredits);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbUserCredits.getId();

        // 调用
        userCreditsService.deleteUserCredits(id);
       // 校验数据不存在了
       assertNull(userCreditsMapper.selectById(id));
    }

    @Test
    public void testDeleteUserCredits_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> userCreditsService.deleteUserCredits(id), USER_CREDITS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetUserCreditsPage() {
       // mock 数据
       UserCreditsDO dbUserCredits = randomPojo(UserCreditsDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setTotalCredits(null);
           o.setUsedCredits(null);
           o.setRemainingCredits(null);
           o.setLastResetTime(null);
           o.setCreateTime(null);
       });
       userCreditsMapper.insert(dbUserCredits);
       // 测试 userId 不匹配
       userCreditsMapper.insert(cloneIgnoreId(dbUserCredits, o -> o.setUserId(null)));
       // 测试 totalCredits 不匹配
       userCreditsMapper.insert(cloneIgnoreId(dbUserCredits, o -> o.setTotalCredits(null)));
       // 测试 usedCredits 不匹配
       userCreditsMapper.insert(cloneIgnoreId(dbUserCredits, o -> o.setUsedCredits(null)));
       // 测试 remainingCredits 不匹配
       userCreditsMapper.insert(cloneIgnoreId(dbUserCredits, o -> o.setRemainingCredits(null)));
       // 测试 lastResetTime 不匹配
       userCreditsMapper.insert(cloneIgnoreId(dbUserCredits, o -> o.setLastResetTime(null)));
       // 测试 createTime 不匹配
       userCreditsMapper.insert(cloneIgnoreId(dbUserCredits, o -> o.setCreateTime(null)));
       // 准备参数
       UserCreditsPageReqVO reqVO = new UserCreditsPageReqVO();
       reqVO.setUserId(null);
       reqVO.setTotalCredits(null);
       reqVO.setUsedCredits(null);
       reqVO.setRemainingCredits(null);
       reqVO.setLastResetTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<UserCreditsDO> pageResult = userCreditsService.getUserCreditsPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbUserCredits, pageResult.getList().get(0));
    }

}