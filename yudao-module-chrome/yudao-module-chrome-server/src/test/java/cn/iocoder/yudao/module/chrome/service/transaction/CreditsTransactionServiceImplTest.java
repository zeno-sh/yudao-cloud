package cn.iocoder.yudao.module.chrome.service.transaction;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.chrome.controller.admin.transaction.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.transaction.CreditsTransactionDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.transaction.CreditsTransactionMapper;
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
 * {@link CreditsTransactionServiceImpl} 的单元测试类
 *
 * @author Jax
 */
@Import(CreditsTransactionServiceImpl.class)
public class CreditsTransactionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CreditsTransactionServiceImpl creditsTransactionService;

    @Resource
    private CreditsTransactionMapper creditsTransactionMapper;

    @Test
    public void testCreateCreditsTransaction_success() {
        // 准备参数
        CreditsTransactionSaveReqVO createReqVO = randomPojo(CreditsTransactionSaveReqVO.class).setId(null);

        // 调用
        Long creditsTransactionId = creditsTransactionService.createCreditsTransaction(createReqVO);
        // 断言
        assertNotNull(creditsTransactionId);
        // 校验记录的属性是否正确
        CreditsTransactionDO creditsTransaction = creditsTransactionMapper.selectById(creditsTransactionId);
        assertPojoEquals(createReqVO, creditsTransaction, "id");
    }

    @Test
    public void testUpdateCreditsTransaction_success() {
        // mock 数据
        CreditsTransactionDO dbCreditsTransaction = randomPojo(CreditsTransactionDO.class);
        creditsTransactionMapper.insert(dbCreditsTransaction);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CreditsTransactionSaveReqVO updateReqVO = randomPojo(CreditsTransactionSaveReqVO.class, o -> {
            o.setId(dbCreditsTransaction.getId()); // 设置更新的 ID
        });

        // 调用
        creditsTransactionService.updateCreditsTransaction(updateReqVO);
        // 校验是否更新正确
        CreditsTransactionDO creditsTransaction = creditsTransactionMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, creditsTransaction);
    }

    @Test
    public void testUpdateCreditsTransaction_notExists() {
        // 准备参数
        CreditsTransactionSaveReqVO updateReqVO = randomPojo(CreditsTransactionSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> creditsTransactionService.updateCreditsTransaction(updateReqVO), CREDITS_TRANSACTION_NOT_EXISTS);
    }

    @Test
    public void testDeleteCreditsTransaction_success() {
        // mock 数据
        CreditsTransactionDO dbCreditsTransaction = randomPojo(CreditsTransactionDO.class);
        creditsTransactionMapper.insert(dbCreditsTransaction);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbCreditsTransaction.getId();

        // 调用
        creditsTransactionService.deleteCreditsTransaction(id);
       // 校验数据不存在了
       assertNull(creditsTransactionMapper.selectById(id));
    }

    @Test
    public void testDeleteCreditsTransaction_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> creditsTransactionService.deleteCreditsTransaction(id), CREDITS_TRANSACTION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCreditsTransactionPage() {
       // mock 数据
       CreditsTransactionDO dbCreditsTransaction = randomPojo(CreditsTransactionDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setTransactionType(null);
           o.setCreditsAmount(null);
           o.setBeforeCredits(null);
           o.setAfterCredits(null);
           o.setBusinessType(null);
           o.setBusinessId(null);
           o.setDescription(null);
           o.setCreateTime(null);
       });
       creditsTransactionMapper.insert(dbCreditsTransaction);
       // 测试 userId 不匹配
       creditsTransactionMapper.insert(cloneIgnoreId(dbCreditsTransaction, o -> o.setUserId(null)));
       // 测试 transactionType 不匹配
       creditsTransactionMapper.insert(cloneIgnoreId(dbCreditsTransaction, o -> o.setTransactionType(null)));
       // 测试 creditsAmount 不匹配
       creditsTransactionMapper.insert(cloneIgnoreId(dbCreditsTransaction, o -> o.setCreditsAmount(null)));
       // 测试 beforeCredits 不匹配
       creditsTransactionMapper.insert(cloneIgnoreId(dbCreditsTransaction, o -> o.setBeforeCredits(null)));
       // 测试 afterCredits 不匹配
       creditsTransactionMapper.insert(cloneIgnoreId(dbCreditsTransaction, o -> o.setAfterCredits(null)));
       // 测试 businessType 不匹配
       creditsTransactionMapper.insert(cloneIgnoreId(dbCreditsTransaction, o -> o.setBusinessType(null)));
       // 测试 businessId 不匹配
       creditsTransactionMapper.insert(cloneIgnoreId(dbCreditsTransaction, o -> o.setBusinessId(null)));
       // 测试 description 不匹配
       creditsTransactionMapper.insert(cloneIgnoreId(dbCreditsTransaction, o -> o.setDescription(null)));
       // 测试 createTime 不匹配
       creditsTransactionMapper.insert(cloneIgnoreId(dbCreditsTransaction, o -> o.setCreateTime(null)));
       // 准备参数
       CreditsTransactionPageReqVO reqVO = new CreditsTransactionPageReqVO();
       reqVO.setUserId(null);
       reqVO.setTransactionType(null);
       reqVO.setCreditsAmount(null);
       reqVO.setBeforeCredits(null);
       reqVO.setAfterCredits(null);
       reqVO.setBusinessType(null);
       reqVO.setBusinessId(null);
       reqVO.setDescription(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<CreditsTransactionDO> pageResult = creditsTransactionService.getCreditsTransactionPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbCreditsTransaction, pageResult.getList().get(0));
    }

}