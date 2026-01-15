package cn.iocoder.yudao.module.chrome.service.usage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.chrome.controller.admin.usage.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.usage.UsageRecordDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.usage.UsageRecordMapper;
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
 * {@link UsageRecordServiceImpl} 的单元测试类
 *
 * @author Jax
 */
@Import(UsageRecordServiceImpl.class)
public class UsageRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private UsageRecordServiceImpl usageRecordService;

    @Resource
    private UsageRecordMapper usageRecordMapper;

    @Test
    public void testCreateUsageRecord_success() {
        // 准备参数
        UsageRecordSaveReqVO createReqVO = randomPojo(UsageRecordSaveReqVO.class).setId(null);

        // 调用
        Long usageRecordId = usageRecordService.createUsageRecord(createReqVO);
        // 断言
        assertNotNull(usageRecordId);
        // 校验记录的属性是否正确
        UsageRecordDO usageRecord = usageRecordMapper.selectById(usageRecordId);
        assertPojoEquals(createReqVO, usageRecord, "id");
    }

    @Test
    public void testUpdateUsageRecord_success() {
        // mock 数据
        UsageRecordDO dbUsageRecord = randomPojo(UsageRecordDO.class);
        usageRecordMapper.insert(dbUsageRecord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        UsageRecordSaveReqVO updateReqVO = randomPojo(UsageRecordSaveReqVO.class, o -> {
            o.setId(dbUsageRecord.getId()); // 设置更新的 ID
        });

        // 调用
        usageRecordService.updateUsageRecord(updateReqVO);
        // 校验是否更新正确
        UsageRecordDO usageRecord = usageRecordMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, usageRecord);
    }

    @Test
    public void testUpdateUsageRecord_notExists() {
        // 准备参数
        UsageRecordSaveReqVO updateReqVO = randomPojo(UsageRecordSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> usageRecordService.updateUsageRecord(updateReqVO), USAGE_RECORD_NOT_EXISTS);
    }

    @Test
    public void testDeleteUsageRecord_success() {
        // mock 数据
        UsageRecordDO dbUsageRecord = randomPojo(UsageRecordDO.class);
        usageRecordMapper.insert(dbUsageRecord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbUsageRecord.getId();

        // 调用
        usageRecordService.deleteUsageRecord(id);
       // 校验数据不存在了
       assertNull(usageRecordMapper.selectById(id));
    }

    @Test
    public void testDeleteUsageRecord_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> usageRecordService.deleteUsageRecord(id), USAGE_RECORD_NOT_EXISTS);
    }

}