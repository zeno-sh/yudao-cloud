package cn.iocoder.yudao.module.chrome.service.email;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.chrome.controller.admin.email.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.email.EmailCodeDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.email.EmailCodeMapper;
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
 * {@link EmailCodeServiceImpl} 的单元测试类
 *
 * @author Jax
 */
@Import(EmailCodeServiceImpl.class)
public class EmailCodeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private EmailCodeServiceImpl emailCodeService;

    @Resource
    private EmailCodeMapper emailCodeMapper;

    @Test
    public void testCreateEmailCode_success() {
        // 准备参数
        EmailCodeSaveReqVO createReqVO = randomPojo(EmailCodeSaveReqVO.class).setId(null);

        // 调用
        Long emailCodeId = emailCodeService.createEmailCode(createReqVO);
        // 断言
        assertNotNull(emailCodeId);
        // 校验记录的属性是否正确
        EmailCodeDO emailCode = emailCodeMapper.selectById(emailCodeId);
        assertPojoEquals(createReqVO, emailCode, "id");
    }

    @Test
    public void testUpdateEmailCode_success() {
        // mock 数据
        EmailCodeDO dbEmailCode = randomPojo(EmailCodeDO.class);
        emailCodeMapper.insert(dbEmailCode);// @Sql: 先插入出一条存在的数据
        // 准备参数
        EmailCodeSaveReqVO updateReqVO = randomPojo(EmailCodeSaveReqVO.class, o -> {
            o.setId(dbEmailCode.getId()); // 设置更新的 ID
        });

        // 调用
        emailCodeService.updateEmailCode(updateReqVO);
        // 校验是否更新正确
        EmailCodeDO emailCode = emailCodeMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, emailCode);
    }

    @Test
    public void testUpdateEmailCode_notExists() {
        // 准备参数
        EmailCodeSaveReqVO updateReqVO = randomPojo(EmailCodeSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> emailCodeService.updateEmailCode(updateReqVO), EMAIL_CODE_NOT_EXISTS);
    }

    @Test
    public void testDeleteEmailCode_success() {
        // mock 数据
        EmailCodeDO dbEmailCode = randomPojo(EmailCodeDO.class);
        emailCodeMapper.insert(dbEmailCode);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbEmailCode.getId();

        // 调用
        emailCodeService.deleteEmailCode(id);
       // 校验数据不存在了
       assertNull(emailCodeMapper.selectById(id));
    }

    @Test
    public void testDeleteEmailCode_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> emailCodeService.deleteEmailCode(id), EMAIL_CODE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetEmailCodePage() {
       // mock 数据
       EmailCodeDO dbEmailCode = randomPojo(EmailCodeDO.class, o -> { // 等会查询到
           o.setEmail(null);
           o.setCode(null);
           o.setScene(null);
           o.setUsed(null);
           o.setExpireTime(null);
           o.setCreateTime(null);
       });
       emailCodeMapper.insert(dbEmailCode);
       // 测试 email 不匹配
       emailCodeMapper.insert(cloneIgnoreId(dbEmailCode, o -> o.setEmail(null)));
       // 测试 code 不匹配
       emailCodeMapper.insert(cloneIgnoreId(dbEmailCode, o -> o.setCode(null)));
       // 测试 scene 不匹配
       emailCodeMapper.insert(cloneIgnoreId(dbEmailCode, o -> o.setScene(null)));
       // 测试 used 不匹配
       emailCodeMapper.insert(cloneIgnoreId(dbEmailCode, o -> o.setUsed(null)));
       // 测试 expireTime 不匹配
       emailCodeMapper.insert(cloneIgnoreId(dbEmailCode, o -> o.setExpireTime(null)));
       // 测试 createTime 不匹配
       emailCodeMapper.insert(cloneIgnoreId(dbEmailCode, o -> o.setCreateTime(null)));
       // 准备参数
       EmailCodePageReqVO reqVO = new EmailCodePageReqVO();
       reqVO.setEmail(null);
       reqVO.setCode(null);
       reqVO.setScene(null);
       reqVO.setUsed(null);
       reqVO.setExpireTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<EmailCodeDO> pageResult = emailCodeService.getEmailCodePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbEmailCode, pageResult.getList().get(0));
    }

}